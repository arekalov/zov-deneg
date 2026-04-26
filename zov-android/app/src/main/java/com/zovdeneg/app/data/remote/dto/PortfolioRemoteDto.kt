package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PortfolioSecurityRemoteDto(
    val id: String,
    val ticker: String,
    val name: String,
)

@Serializable
internal data class PortfolioItemRemoteDto(
    val securityId: String,
    val security: PortfolioSecurityRemoteDto,
    val quantity: Int,
    val averagePrice: String,
    val currentPrice: String,
    val currentValue: String,
    val profitLoss: String,
    val profitLossPct: String,
)

@Serializable
internal data class PortfolioRemoteDto(
    val totalValue: String,
    val securitiesValue: String,
    val cashBalance: String,
    val dailyChange: String,
    val dailyChangePct: String,
    val totalProfitLoss: String,
    val items: List<PortfolioItemRemoteDto>,
)
