package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class BalanceDto(
    val available: String,
    val total: String,
    val blocked: String,
)

@Serializable
internal data class BalanceAmountRequestDto(
    val amount: String,
)
