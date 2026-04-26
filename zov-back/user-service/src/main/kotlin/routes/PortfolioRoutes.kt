package zov.deneg.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import zov.deneg.data.PortfolioRepository
import zov.deneg.models.*
import java.math.BigDecimal

fun Routing.configurePortfolioRoutes(portfolioRepository: PortfolioRepository) {

    // GET /portfolio - Full portfolio with items
    authenticate {
        get("/portfolio") {
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

            val items = portfolioRepository.getAll(java.util.UUID.fromString(userId))

            // Calculate portfolio totals
            val securitiesValue = items.sumOf { item ->
                BigDecimal(item.currentValue)
            }

            // Get cash balance (would need to fetch from balance service)
            val cashBalance = "0.00" // Placeholder - should fetch from balance

            val totalValue = securitiesValue.add(BigDecimal(cashBalance))

            // Calculate daily change and profit/loss
            val totalProfitLoss = items.sumOf { item ->
                BigDecimal(item.profitLoss)
            }

            val dailyChange = BigDecimal.ZERO // Would need historical data
            val dailyChangePct = BigDecimal.ZERO
            val totalProfitLossPct = if (securitiesValue.compareTo(BigDecimal.ZERO) != 0) {
                totalProfitLoss.divide(securitiesValue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            } else {
                BigDecimal.ZERO
            }

            val portfolio = Portfolio(
                totalValue = totalValue.toPlainString(),
                securitiesValue = securitiesValue.toPlainString(),
                cashBalance = cashBalance,
                dailyChange = dailyChange.toPlainString(),
                dailyChangePct = dailyChangePct.toPlainString(),
                totalProfitLoss = totalProfitLoss.toPlainString(),
                items = items
            )

            call.respond(HttpStatusCode.OK, portfolio)
        }

        // GET /portfolio/summary - Portfolio summary
        get("/portfolio/summary") {
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

            val items = portfolioRepository.getAll(java.util.UUID.fromString(userId))

            // Calculate totals
            val totalValue = items.sumOf { item ->
                BigDecimal(item.currentValue)
            }

            val totalProfitLoss = items.sumOf { item ->
                BigDecimal(item.profitLoss)
            }

            val totalProfitLossPct = if (totalValue.compareTo(BigDecimal.ZERO) != 0) {
                totalProfitLoss.divide(totalValue, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
            } else {
                BigDecimal.ZERO
            }

            val summary = PortfolioSummary(
                totalValue = totalValue.toPlainString(),
                profitLoss = totalProfitLoss.toPlainString(),
                profitLossPct = totalProfitLossPct.toPlainString()
            )

            call.respond(HttpStatusCode.OK, summary)
        }
    }
}
