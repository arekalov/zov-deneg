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

// ── Portfolio ──────────────────────────────────────────────────────────────

@Serializable
data class PortfolioItem(
    val securityId: String,
    val security: Security,
    val quantity: Int,
    val averagePrice: String,
    val currentPrice: String,
    val currentValue: String,
    val profitLoss: String,
    val profitLossPct: String
)

@Serializable
data class Portfolio(
    val totalValue: String,
    val securitiesValue: String,
    val cashBalance: String,
    val dailyChange: String,
    val dailyChangePct: String,
    val totalProfitLoss: String,
    val items: List<PortfolioItem>
)

@Serializable
data class PortfolioSummary(
    val totalValue: String,
    val profitLoss: String,
    val profitLossPct: String
)

// ── Orders ──────────────────────────────────────────────────────────────

@Serializable
enum class OrderType {
    @SerialName("market")
    MARKET
}

@Serializable
enum class OrderSide {
    @SerialName("buy")
    BUY,

    @SerialName("sell")
    SELL
}

@Serializable
enum class OrderStatus {
    @SerialName("pending")
    PENDING,

    @SerialName("executed")
    EXECUTED,

    @SerialName("partial")
    PARTIAL,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("rejected")
    REJECTED
}

@Serializable
data class Order(
    val id: String,
    val securityId: String,
    val ticker: String,
    val type: OrderType,
    val side: OrderSide,
    val status: OrderStatus,
    val quantity: Int,
    val executedPrice: String? = null,
    val executedQuantity: Int? = null,
    val totalAmount: String? = null,
    val commission: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class CreateOrderRequest(
    val securityId: String,
    val side: OrderSide,
    val quantity: Int
)

@Serializable
data class OrdersListResponse(
    val data: List<Order>,
    val pagination: PaginationResponse
)
