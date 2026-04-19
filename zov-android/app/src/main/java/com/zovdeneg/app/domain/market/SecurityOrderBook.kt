package com.zovdeneg.app.domain.market

/**
 * Снимок стакана для экрана деталей (уровни в порядке отображения сверху вниз).
 * Цены — в формате API [openapi.yaml] `DecimalString` (точка как разделитель дроби).
 */
data class SecurityOrderBookRow(
    val leftQuantity: Int,
    val priceDecimal: String,
    val rightQuantity: Int?,
)

data class SecurityOrderBook(
    val askLevels: List<SecurityOrderBookRow>,
    val bestAskPriceDecimal: String,
    val bidLevels: List<SecurityOrderBookRow>,
    val bestBidPriceDecimal: String,
)
