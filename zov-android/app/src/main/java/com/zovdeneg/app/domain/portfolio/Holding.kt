package com.zovdeneg.app.domain.portfolio

data class Holding(
    val ticker: String,
    val subtitle: String,
    val valueText: String,
    val deltaText: String,
    val deltaPositive: Boolean,
    /** Количество бумаг в портфеле (штуки), для UI и сверки с деталями инструмента. */
    val quantity: Int,
    /** UUID для zov-back или тикер для мок-HTTP. */
    val detailNavKey: String = ticker,
    /** Как в OpenAPI `Security.type`: `stock`, `bond`, `etf`. */
    val securityTypeKey: String = "stock",
)
