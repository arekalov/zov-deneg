package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PortfolioSummaryDto(
    val totalValue: String,
    val profitLoss: String,
    val profitLossPct: String,
)
