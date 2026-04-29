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
    /** Текущая оценка позиции (все шт.), из портфеля. */
    val portfolioCurrentValueLine: String? = null,
    /** P/L позиции: «±N ₽ (±M%)». */
    val portfolioPositionDeltaLine: String? = null,
    val portfolioPositionDeltaPositive: Boolean? = null,
    /** Рыночная цена одной штуки из портфеля (если есть). */
    val portfolioUnitPriceLine: String? = null,
) {
    /** Есть ли в портфеле хотя бы один полный лот для рыночной продажи. */
    val canSellAtLeastOneLot: Boolean
        get() {
            val q = portfolioQuantity ?: return false
            return lotSize > 0 && q >= lotSize
        }

    /**
     * Краткое имя для заголовка (например облигация без « · MOEX» в подзаголовке).
     * Если подзаголовка нет — тикер.
     */
    fun toolbarDisplayName(): String {
        val s = subtitle.trim()
        if (s.isEmpty()) return ticker
        val sep = " · "
        val i = s.indexOf(sep)
        return if (i >= 0) {
            s.substring(0, i).trim().ifEmpty { ticker }
        } else {
            s
        }
    }
}
