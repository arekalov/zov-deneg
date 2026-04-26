package zov.deneg.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import zov.deneg.repository.PortfolioRepository
import zov.deneg.model.ErrorResponse
import java.util.*

fun Routing.configurePortfolioRoutes(portfolioRepository: PortfolioRepository) {

    // GET /portfolio - Full portfolio with positions
    authenticate {
        get("/portfolio") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.payload?.subject ?: run {
                call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                    code = "UNAUTHORIZED",
                    message = "Требуется аутентификация"
                ))
                return@get
            }

            try {
                val portfolio = portfolioRepository.getPortfolio(UUID.fromString(userId))
                call.respond(HttpStatusCode.OK, portfolio)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                )
            }
        }

        // GET /portfolio/summary - Portfolio summary for main screen
        authenticate {
            get("/portfolio/summary") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal?.payload?.subject ?: run {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                        code = "UNAUTHORIZED",
                        message = "Требуется аутентификация"
                    ))
                    return@get
                }

                try {
                    val summary = portfolioRepository.getPortfolioSummary(UUID.fromString(userId))
                    call.respond(HttpStatusCode.OK, summary)
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
