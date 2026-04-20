package zov.deneg.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import zov.deneg.model.*
import zov.deneg.repository.OrderRepository
import zov.deneg.repository.SecuritiesRepository
import java.util.*

fun Routing.configureOrderRoutes(orderRepository: OrderRepository, securitiesRepository: SecuritiesRepository) {

    // GET /orders - List user orders
    authenticate {
        get("/orders") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject ?: run {
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

            try {
                val (orders, total) = orderRepository.getOrdersByUserId(
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
                    pagination = PaginationResponse(
                        page = page,
                        pageSize = pageSize,
                        totalItems = total,
                        totalPages = totalPages
                    )
                ))
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                )
            }
        }

        // POST /orders - Create new order
        authenticate {
            post("/orders") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject ?: run {
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

                // Get security to check lot size and get ticker
                val securityId = try {
                    UUID.fromString(request.securityId)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        code = "INVALID_SECURITY_ID",
                        message = "Некорректный формат ID ценной бумаги"
                    ))
                    return@post
                }

                val security = securitiesRepository.getSecurityById(securityId)
                if (security == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse(
                        code = "NOT_FOUND",
                        message = "Ценная бумага не найдена"
                    ))
                    return@post
                }

                // Check if quantity is multiple of lot size
                if (request.quantity % security.lotSize != 0) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        code = "INVALID_LOT_SIZE",
                        message = "Количество должно быть кратно размеру лота (${security.lotSize})"
                    ))
                    return@post
                }

                try {
                    val order = orderRepository.createOrder(
                        userId = UUID.fromString(userId),
                        securityId = securityId,
                        ticker = security.ticker,
                        type = OrderType.MARKET,
                        side = request.side,
                        quantity = request.quantity
                    )

                    call.respond(HttpStatusCode.Created, order)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                    )
                }
            }
        }

        // GET /orders/{orderId} - Get order by ID
        authenticate {
            get("/orders/{orderId}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject ?: run {
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

                try {
                    val order = orderRepository.getOrderById(orderId)
                    if (order == null) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(
                            code = "NOT_FOUND",
                            message = "Заявка не найдена"
                        ))
                        return@get
                    }

                    // In production, check if order belongs to user
                    call.respond(HttpStatusCode.OK, order)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                    )
                }
            }
        }

        // DELETE /orders/{orderId} - Cancel order
        authenticate {
            delete("/orders/{orderId}") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject ?: run {
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

                try {
                    val cancelled = orderRepository.cancelOrder(orderId)
                    if (!cancelled) {
                        call.respond(HttpStatusCode.NotFound, ErrorResponse(
                            code = "NOT_FOUND",
                            message = "Заявка не найдена или уже исполнена/отменена"
                        ))
                        return@delete
                    }

                    call.respond(HttpStatusCode.NoContent)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                    )
                }
            }
        }
    }
}
