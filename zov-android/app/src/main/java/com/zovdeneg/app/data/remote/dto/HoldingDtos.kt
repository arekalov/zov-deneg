package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class HoldingDto(
    val ticker: String,
    val subtitle: String,
    val valueText: String,
    val deltaText: String,
    val deltaPositive: Boolean,
)

@Serializable
internal data class HoldingsEnvelopeDto(
    val holdings: List<HoldingDto>,
)
