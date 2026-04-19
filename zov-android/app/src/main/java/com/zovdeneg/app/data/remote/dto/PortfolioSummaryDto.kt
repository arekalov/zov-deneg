package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PortfolioSummaryDto(
    val portfolioAmountRub: String,
    val totalGainText: String,
)
