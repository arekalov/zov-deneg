package com.zovdeneg.app.domain.portfolio

data class Holding(
    val ticker: String,
    val subtitle: String,
    val valueText: String,
    val deltaText: String,
    val deltaPositive: Boolean,
)
