package zov.deneg

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import zov.deneg.model.*
import zov.deneg.repository.SecuritiesRepository
import java.util.*

fun Application.configureRouting() {
    // Configure JSON serialization
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    routing {
        // Health check
        get("/") {
            call.respondText("Securities Service API")
        }

        // ── SECURITIES LIST ──────────────────────────────────────────────────────
        get("/securities") {
            try {
                val dataSource = application.attributes[ClickHouseDataSourceKey]
                val repository = SecuritiesRepository(dataSource)

                val query = call.request.queryParameters["q"]
                val type = call.request.queryParameters["type"]?.let {
                    try { SecurityType.valueOf(it.uppercase()) } catch (e: IllegalArgumentException) { null }
                }
                val exchange = call.request.queryParameters["exchange"]?.let {
                    try { Exchange.valueOf(it) } catch (e: IllegalArgumentException) { null }
                }
                val sector = call.request.queryParameters["sector"]?.let {
                    try { SecuritySector.valueOf(it.replace(' ', '_').uppercase()) } catch (e: IllegalArgumentException) { null }
                }
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 20

                val result = repository.getSecurities(
                    query = query,
                    type = type,
                    exchange = exchange,
                    sector = sector,
                    page = page.coerceAtLeast(1),
                    pageSize = pageSize.coerceIn(1, 100)
                )
                val securities = result.first
                val totalItems = result.second

                val totalPages = (totalItems + pageSize - 1) / pageSize

                call.respond(
                    HttpStatusCode.OK,
                    SecuritiesListResponse(
                        data = securities,
                        pagination = PaginationResponse(
                            page = page,
                            pageSize = pageSize,
                            totalItems = totalItems,
                            totalPages = totalPages
                        )
                    )
                )
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                )
            }
        }

        // ── SECURITY BY ID ──────────────────────────────────────────────────────
        get("/securities/{securityId}") {
            try {
                val securityIdParam = call.parameters["securityId"]
                val securityId = try {
                    UUID.fromString(securityIdParam)
                } catch (e: IllegalArgumentException) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid security ID format")
                    )
                }

                val dataSource = application.attributes[ClickHouseDataSourceKey]
                val repository = SecuritiesRepository(dataSource)

                val security = repository.getSecurityById(securityId)
                if (security != null) {
                    call.respond(HttpStatusCode.OK, security)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Security not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                )
            }
        }

        // ── PRICE HISTORY ──────────────────────────────────────────────────────
        get("/securities/{securityId}/price/history") {
            try {
                val securityIdParam = call.parameters["securityId"]
                val securityId = try {
                    UUID.fromString(securityIdParam)
                } catch (e: IllegalArgumentException) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid security ID format")
                    )
                }

                val fromParam = call.request.queryParameters["from"]?.toLongOrNull()
                val toParam = call.request.queryParameters["to"]?.toLongOrNull()

                if (fromParam == null || toParam == null) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_DATE_RANGE", "Parameters 'from' and 'to' are required")
                    )
                }

                if (fromParam > toParam) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_DATE_RANGE", "Parameter 'from' cannot be greater than 'to'")
                    )
                }

                val dataSource = application.attributes[ClickHouseDataSourceKey]
                val repository = SecuritiesRepository(dataSource)

                val history = repository.getPriceHistory(securityId, fromParam, toParam)
                if (history != null) {
                    call.respond(HttpStatusCode.OK, history)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Security not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                )
            }
        }

        // ── ORDER BOOK ──────────────────────────────────────────────────────
        get("/securities/{securityId}/orderbook") {
            try {
                val securityIdParam = call.parameters["securityId"]
                val securityId = try {
                    UUID.fromString(securityIdParam)
                } catch (e: IllegalArgumentException) {
                    return@get call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("INVALID_ID", "Invalid security ID format")
                    )
                }

                val depth = call.request.queryParameters["depth"]?.toIntOrNull()?.coerceIn(1, 50) ?: 10

                val dataSource = application.attributes[ClickHouseDataSourceKey]
                val repository = SecuritiesRepository(dataSource)

                val orderBook = repository.getOrderBook(securityId, depth)
                if (orderBook != null) {
                    call.respond(HttpStatusCode.OK, orderBook)
                } else {
                    call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse("NOT_FOUND", "Security not found")
                    )
                }
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    ErrorResponse("INTERNAL_ERROR", e.message ?: "Internal server error")
                )
            }
        }
    }
}
