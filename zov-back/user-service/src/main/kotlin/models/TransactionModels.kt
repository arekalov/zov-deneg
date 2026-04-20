package zov.deneg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Transactions ──────────────────────────────────────────────────────────────

@Serializable
enum class TransactionType {
    @SerialName("buy")
    BUY,

    @SerialName("sell")
    SELL,

    @SerialName("deposit")
    DEPOSIT,

    @SerialName("withdrawal")
    WITHDRAWAL,

    @SerialName("dividend")
    DIVIDEND
}

@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val securityId: String? = null,
    val ticker: String? = null,
    val securityName: String? = null,
    val quantity: Int? = null,
    val price: String? = null,
    val amount: String,
    val commission: String? = null,
    val orderId: String? = null,
    val createdAt: Long
)

@Serializable
data class TransactionsListResponse(
    val data: List<Transaction>,
    val pagination: Pagination
)
