package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class SecurityOrderBookLevelDto(
    val quantity: Int,
    val price: String,
    val mirrorQuantity: Int? = null,
)

@Serializable
internal data class SecurityOrderBookDto(
    val askLevels: List<SecurityOrderBookLevelDto> = emptyList(),
    val bestAskPrice: String,
    val bestBidPrice: String,
    val bidLevels: List<SecurityOrderBookLevelDto> = emptyList(),
)
