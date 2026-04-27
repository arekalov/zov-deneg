package com.zovdeneg.app.domain.orders

data class UserOrder(
    val id: String,
    val securityId: String,
    val ticker: String,
    val side: String,
    val status: String,
    val quantity: Int,
    val executedPrice: String?,
    val executedQuantity: Int?,
    val totalAmount: String?,
    val commission: String?,
    val createdAtEpochSeconds: Long,
    val updatedAtEpochSeconds: Long,
) {
    fun isCancellable(): Boolean = status == "pending"
}
