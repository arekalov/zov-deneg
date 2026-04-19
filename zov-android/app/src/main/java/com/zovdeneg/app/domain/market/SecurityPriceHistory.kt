package com.zovdeneg.app.domain.market

data class PriceHistoryPoint(
    val timestampSeconds: Long,
    val price: Double,
)

data class SecurityPriceHistory(
    val securityId: String,
    val ticker: String,
    val fromSeconds: Long,
    val toSeconds: Long,
    val points: List<PriceHistoryPoint>,
)
