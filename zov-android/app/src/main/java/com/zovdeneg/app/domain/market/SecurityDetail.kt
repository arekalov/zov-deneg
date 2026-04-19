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
)
