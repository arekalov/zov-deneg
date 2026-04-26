package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class SecuritiesListRemoteDto(
    val data: List<SecurityCardRemoteDto>,
    val pagination: PaginationRemoteDto,
)

@Serializable
internal data class SecurityCardRemoteDto(
    val id: String,
    val ticker: String,
    val name: String,
    val description: String? = null,
    val type: String,
    val exchange: String,
    val sector: String,
    val lotSize: Int,
    val lastPrice: String,
    val priceChange: String,
    val priceChangePct: String,
)

@Serializable
internal data class OrderBookLevelRemoteDto(
    val price: String,
    val quantity: Int,
)

@Serializable
internal data class OrderBookRemoteDto(
    val securityId: String,
    val ticker: String,
    val timestamp: Long,
    val asks: List<OrderBookLevelRemoteDto>,
    val bids: List<OrderBookLevelRemoteDto>,
    val spread: String,
)
