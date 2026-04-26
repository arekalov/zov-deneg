package zov.deneg.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import zov.deneg.data.BalanceRepository
import zov.deneg.data.TransactionRepository
import zov.deneg.models.*
import java.math.BigDecimal
import java.util.*

fun Routing.configureBalanceRoutes(balanceRepository: BalanceRepository, transactionRepository: TransactionRepository) {

    // GET /balance - Current user balance
    authenticate {
        get("/balance") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@get
            }

            val balance = balanceRepository.getBalance(UUID.fromString(userId))
            call.respond(HttpStatusCode.OK, balance)
        }

        // POST /balance/deposit - Deposit funds
        authenticate {
            post("/balance/deposit") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        code = "UNAUTHORIZED",
                        message = "Требуется аутентификация"
                    ))
                    return@post
                }

                val request = try {
                    call.receive<DepositRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = "Некорректный формат запроса"
                    ))
                    return@post
                }

                // Validate amount
                val amount = try {
                    val bd = BigDecimal(request.amount)
                    if (bd <= BigDecimal.ZERO) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            code = "INVALID_AMOUNT",
                            message = "Сумма должна быть больше нуля"
                        ))
                        return@post
                    }
                    if (bd < BigDecimal.ONE) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            code = "INVALID_AMOUNT",
                            message = "Минимальная сумма пополнения — 1 рубль"
                        ))
                        return@post
                    }
                    bd
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        code = "INVALID_AMOUNT",
                        message = "Некорректный формат суммы"
                    ))
                    return@post
                }

                val balance = balanceRepository.deposit(UUID.fromString(userId), amount)

                // Create transaction record
                transactionRepository.create(
                    userId = UUID.fromString(userId),
                    type = TransactionType.DEPOSIT,
                    amount = amount,
                    commission = BigDecimal.ZERO
                )

                call.respond(HttpStatusCode.OK, balance)
            }
        }

        // POST /balance/withdraw - Withdraw funds
        authenticate {
            post("/balance/withdraw") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        code = "UNAUTHORIZED",
                        message = "Требуется аутентификация"
                    ))
                    return@post
                }

                val request = try {
                    call.receive<WithdrawRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        code = "INVALID_REQUEST",
                        message = "Некорректный формат запроса"
                    ))
                    return@post
                }

                // Validate amount
                val amount = try {
                    val bd = BigDecimal(request.amount)
                    if (bd <= BigDecimal.ZERO) {
                        call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                            code = "INVALID_AMOUNT",
                            message = "Сумма должна быть больше нуля"
                        ))
                        return@post
                    }
                    bd
                } catch (e: NumberFormatException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse(
                        code = "INVALID_AMOUNT",
                        message = "Некорректный формат суммы"
                    ))
                    return@post
                }

                val balance = balanceRepository.withdraw(UUID.fromString(userId), amount)
                if (balance == null) {
                    call.respond(HttpStatusCode.UnprocessableEntity, ErrorResponse(
                        code = "INSUFFICIENT_FUNDS",
                        message = "Недостаточно средств на счёте"
                    ))
                    return@post
                }

                // Create transaction record
                transactionRepository.create(
                    userId = UUID.fromString(userId),
                    type = TransactionType.WITHDRAWAL,
                    amount = amount.negate(),
                    commission = BigDecimal.ZERO
                )

                call.respond(HttpStatusCode.OK, balance)
            }
        }
    }
}
