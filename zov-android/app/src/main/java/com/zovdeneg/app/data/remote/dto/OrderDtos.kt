package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class CreateOrderRequestDto(
    val securityId: String,
    val side: String,
    val quantity: Int,
)

@Serializable
internal data class OrderResponseDto(
    val id: String,
    val securityId: String,
    val ticker: String? = null,
    val type: String = "market",
    val side: String,
    val status: String,
    val quantity: Int,
    val executedPrice: String? = null,
    val executedQuantity: Int? = null,
    val totalAmount: String? = null,
    val commission: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
)
