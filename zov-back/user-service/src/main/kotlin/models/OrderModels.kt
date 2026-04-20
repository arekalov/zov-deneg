package zov.deneg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    val pagination: Pagination
)
