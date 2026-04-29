package zov.deneg.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import zov.deneg.client.SecuritiesClient
import zov.deneg.data.OrderRepository
import zov.deneg.data.PortfolioRepository
import zov.deneg.data.BalanceRepository
import zov.deneg.data.TransactionRepository
import zov.deneg.models.*
import java.math.BigDecimal
import java.util.*

fun Routing.configureOrderRoutes(
    orderRepository: OrderRepository,
    portfolioRepository: PortfolioRepository,
    balanceRepository: BalanceRepository,
    securitiesClient: SecuritiesClient,
    transactionRepository: TransactionRepository
) {

    // GET /orders - List user orders with filters
    authenticate {
        get("/orders") {
            val principal = call.principal<JWTPrincipal>() ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@get
            }
            val userId = principal.payload.subject ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@get
            }

            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20
            val statusParam = call.request.queryParameters["status"]
            val sideParam = call.request.queryParameters["side"]
            val securityIdParam = call.request.queryParameters["securityId"]

            val status = statusParam?.let {
                try { OrderStatus.valueOf(it.uppercase()) } catch (e: IllegalArgumentException) { null }
            }

            val side = sideParam?.let {
                try { OrderSide.valueOf(it.uppercase()) } catch (e: IllegalArgumentException) { null }
            }

            val securityId = securityIdParam?.let {
                try { UUID.fromString(it) } catch (e: IllegalArgumentException) { null }
            }

            val (orders, total) = orderRepository.findByUserId(
                userId = UUID.fromString(userId),
                page = page.coerceAtLeast(1),
                pageSize = pageSize.coerceIn(1, 100),
                status = status,
                side = side,
                securityId = securityId
            )

            val totalPages = (total + pageSize - 1) / pageSize

            call.respond(HttpStatusCode.OK, OrdersListResponse(
                data = orders,
                pagination = Pagination(
                    page = page,
                    pageSize = pageSize,
                    totalItems = total,
                    totalPages = totalPages
                )
            ))
        }

        // POST /orders - Create and instantly execute order
        post("/orders") {
            val principal = call.principal<JWTPrincipal>() ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@post
            }
            val userId = principal.payload.subject ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@post
            }

            val request = try {
                call.receive<CreateOrderRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_REQUEST",
                    message = "Некорректный формат запроса"
                ))
                return@post
            }

            // Validate quantity
            if (request.quantity < 1) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_QUANTITY",
                    message = "Количество должно быть больше нуля"
                ))
                return@post
            }

            // Parse security ID
            val securityId = try {
                UUID.fromString(request.securityId)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_SECURITY_ID",
                    message = "Некорректный формат ID инструмента"
                ))
                return@post
            }

            // Fetch security details from securities service
            val security = securitiesClient.getSecurityById(securityId)
            if (security == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    code = "SECURITY_NOT_FOUND",
                    message = "Инструмент не найден"
                ))
                return@post
            }

            // Parse price from security
            val price = try {
                BigDecimal(security.lastPrice)
            } catch (e: NumberFormatException) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    code = "INVALID_PRICE",
                    message = "Некорректная цена инструмента"
                ))
                return@post
            }

            val totalAmount = price.multiply(BigDecimal.valueOf(request.quantity.toLong()))
            val commission = totalAmount.multiply(BigDecimal("0.001")) // 0.1% commission

            // Execute order based on side (BUY/SELL)
            try {
                if (request.side == OrderSide.BUY) {
                    // Check if user has sufficient balance
                    val balance = balanceRepository.getBalance(UUID.fromString(userId))
                    val requiredAmount = totalAmount.add(commission)
                    
                    if (BigDecimal(balance.available) < requiredAmount) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            code = "INSUFFICIENT_FUNDS",
                            message = "Недостаточно средств для покупки. Требуется: $requiredAmount, Доступно: ${balance.available}"
                        ))
                        return@post
                    }

                    // Withdraw funds from balance
                    balanceRepository.withdraw(UUID.fromString(userId), requiredAmount)

                    // Update portfolio
                    portfolioRepository.updateAfterTrade(
                        userId = UUID.fromString(userId),
                        securityId = securityId,
                        ticker = security.ticker,
                        securityName = security.name,
                        quantity = request.quantity,
                        price = price,
                        isBuy = true
                    )

                    // Create and execute order in single operation
                    val order = orderRepository.createAndExecute(
                        userId = UUID.fromString(userId),
                        securityId = securityId,
                        ticker = security.ticker,
                        type = OrderType.MARKET,
                        side = request.side,
                        quantity = request.quantity,
                        executedPrice = price,
                        totalAmount = totalAmount,
                        commission = commission
                    )

                    if (order == null) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                            code = "INTERNAL_ERROR",
                            message = "Не удалось создать заявку"
                        ))
                        return@post
                    }

                    // Create transaction record
                    transactionRepository.create(
                        userId = UUID.fromString(userId),
                        type = TransactionType.BUY,
                        amount = requiredAmount,
                        securityId = securityId,
                        ticker = security.ticker,
                        securityName = security.name,
                        quantity = request.quantity,
                        price = price,
                        commission = commission,
                        orderId = UUID.fromString(order.id)
                    )

                    call.respond(HttpStatusCode.Created, order)

                } else {
                    // SELL - Check if user has the securities in portfolio
                    val portfolioItem = portfolioRepository.get(UUID.fromString(userId), securityId)
                    val availableQty = portfolioItem?.quantity ?: 0

                    if (availableQty < request.quantity) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            code = "INSUFFICIENT_SECURITIES",
                            message = "Недостаточно ценных бумаг. Требуется: $request.quantity, Доступно: $availableQty"
                        ))
                        return@post
                    }

                    // Credit funds to balance (minus commission)
                    val netAmount = totalAmount.subtract(commission)
                    balanceRepository.deposit(UUID.fromString(userId), netAmount)

                    // Update portfolio (reduce position)
                    portfolioRepository.updateAfterTrade(
                        userId = UUID.fromString(userId),
                        securityId = securityId,
                        ticker = security.ticker,
                        securityName = security.name,
                        quantity = request.quantity,
                        price = price,
                        isBuy = false
                    )

                    // Create and execute order in single operation
                    val order = orderRepository.createAndExecute(
                        userId = UUID.fromString(userId),
                        securityId = securityId,
                        ticker = security.ticker,
                        type = OrderType.MARKET,
                        side = request.side,
                        quantity = request.quantity,
                        executedPrice = price,
                        totalAmount = totalAmount,
                        commission = commission
                    )

                    if (order == null) {
                        call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                            code = "INTERNAL_ERROR",
                            message = "Не удалось создать заявку"
                        ))
                        return@post
                    }

                    // Create transaction record
                    transactionRepository.create(
                        userId = UUID.fromString(userId),
                        type = TransactionType.SELL,
                        amount = netAmount,
                        securityId = securityId,
                        ticker = security.ticker,
                        securityName = security.name,
                        quantity = request.quantity,
                        price = price,
                        commission = commission,
                        orderId = UUID.fromString(order.id)
                    )

                    call.respond(HttpStatusCode.Created, order)
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                    code = "EXECUTION_ERROR",
                    message = "Ошибка при исполнении заявки: ${e.message}"
                ))
                return@post
            }
        }

        // GET /orders/{orderId} - Get order details
        get("/orders/{orderId}") {
            val principal = call.principal<JWTPrincipal>() ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@get
            }
            val userId = principal.payload.subject ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@get
            }

            val orderId = call.parameters["orderId"]?.let {
                try { UUID.fromString(it) } catch (e: IllegalArgumentException) { null }
            }

            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_ID",
                    message = "Некорректный формат ID"
                ))
                return@get
            }

            val order = orderRepository.findByIdAndUserId(orderId, UUID.fromString(userId))
            if (order == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    code = "NOT_FOUND",
                    message = "Заявка не найдена"
                ))
                return@get
            }

            call.respond(HttpStatusCode.OK, order)
        }

        // DELETE /orders/{orderId} - Cancel order
        delete("/orders/{orderId}") {
            val principal = call.principal<JWTPrincipal>() ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@delete
            }
            val userId = principal.payload.subject ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@delete
            }

            val orderId = call.parameters["orderId"]?.let {
                try { UUID.fromString(it) } catch (e: IllegalArgumentException) { null }
            }

            if (orderId == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "INVALID_ID",
                    message = "Некорректный формат ID"
                ))
                return@delete
            }

            val order = orderRepository.findByIdAndUserId(orderId, UUID.fromString(userId))
            if (order == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(
                    code = "NOT_FOUND",
                    message = "Заявка не найдена"
                ))
                return@delete
            }

            val cancelledOrder = orderRepository.cancel(orderId)
            if (cancelledOrder == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                    code = "CANNOT_CANCEL",
                    message = "Невозможно отменить заявку в текущем статусе"
                ))
                return@delete
            }

            call.respond(HttpStatusCode.NoContent)
        }
    }
}
