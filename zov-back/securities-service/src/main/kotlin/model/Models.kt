package zov.deneg.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

// ── Enums ──────────────────────────────────────────────────────────────

@Serializable
enum class SecurityType {
    @SerialName("stock")
    STOCK,
    
    @SerialName("bond")
    BOND,
    
    @SerialName("etf")
    ETF
}

@Serializable
enum class Exchange {
    @SerialName("MOEX")
    MOEX,
    
    @SerialName("SPB")
    SPB
}

@Serializable
enum class SecuritySector {
    @SerialName("Финансы")
    FINANCE,
    
    @SerialName("Энергетика")
    ENERGY,
    
    @SerialName("Металлургия")
    METALLURGY,
    
    @SerialName("Телекоммуникации")
    TELECOM,
    
    @SerialName("Потребительский сектор")
    CONSUMER,
    
    @SerialName("Информационные технологии")
    IT,
    
    @SerialName("Транспорт")
    TRANSPORT,
    
    @SerialName("Химическая промышленность")
    CHEMICAL,
    
    @SerialName("Строительство")
    CONSTRUCTION,
    
    @SerialName("Другое")
    OTHER
}

// ── Common ──────────────────────────────────────────────────────────────

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

@Serializable
data class PaginationResponse(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int
)

// ── Security ──────────────────────────────────────────────────────────────

@Serializable
data class Security(
    val id: String,
    val ticker: String,
    val name: String,
    val description: String? = null,
    val type: SecurityType,
    val exchange: Exchange,
    val sector: SecuritySector,
    val lotSize: Int,
    val lastPrice: String,
    val priceChange: String,
    val priceChangePct: String
)

// ── Price History ──────────────────────────────────────────────────────────────

@Serializable
data class PricePoint(
    val timestamp: Long,
    val price: String
)

@Serializable
data class PriceHistoryResponse(
    val securityId: String,
    val ticker: String,
    val from: Long,
    val to: Long,
    val data: List<PricePoint>
)

// ── Order Book ──────────────────────────────────────────────────────────────

@Serializable
data class OrderBookLevel(
    val price: String,
    val quantity: Int
)

@Serializable
data class OrderBookResponse(
    val securityId: String,
    val ticker: String,
    val timestamp: Long,
    val asks: List<OrderBookLevel>,
    val bids: List<OrderBookLevel>,
    val spread: String
)

// ── List Response ──────────────────────────────────────────────────────────────

@Serializable
data class SecuritiesListResponse(
    val data: List<Security>,
    val pagination: PaginationResponse
)
