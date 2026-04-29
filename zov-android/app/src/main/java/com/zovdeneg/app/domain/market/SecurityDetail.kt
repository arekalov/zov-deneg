package com.zovdeneg.app.domain.market

data class SecurityDetail(
    val ticker: String,
    val subtitle: String,
    val priceLine: String,
    val changeLine: String,
    val changePositive: Boolean,
    val securityId: String,
    val lotSize: Int,
    val orderBookText: String?,
    val orderBook: SecurityOrderBook? = null,
    val sectorName: String = "",
    val exchangeCode: String = "",
    val companyDescription: String? = null,
    val portfolioQuantity: Int? = null,
    val portfolioAvgPriceLine: String? = null,
) {
    /** Есть ли в портфеле хотя бы один полный лот для рыночной продажи. */
    val canSellAtLeastOneLot: Boolean
        get() {
            val q = portfolioQuantity ?: return false
            return lotSize > 0 && q >= lotSize
        }
}
