package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PopularSecurityDto(
    val ticker: String,
    val subtitle: String,
    val valueText: String,
    val deltaText: String,
    val deltaPositive: Boolean,
    val kind: String,
)

@Serializable
internal data class PopularSecuritiesEnvelopeDto(
    val items: List<PopularSecurityDto>,
)
