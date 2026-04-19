package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PricePointDto(
    val timestamp: Long,
    val price: String,
)

@Serializable
internal data class SecurityPriceHistoryDto(
    val securityId: String,
    val ticker: String,
    val from: Long,
    val to: Long,
    val data: List<PricePointDto>,
)
