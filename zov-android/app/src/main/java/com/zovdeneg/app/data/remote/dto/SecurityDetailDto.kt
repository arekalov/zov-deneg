package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class SecurityDetailDto(
    val ticker: String,
    val subtitle: String,
    val priceLine: String,
    val changeLine: String,
    val changePositive: Boolean,
    val securityId: String = "",
    val lotSize: Int = 10,
    val orderBookText: String? = null,
    val sectorName: String = "",
    val exchangeCode: String = "",
    val companyDescription: String? = null,
    val portfolioQuantity: Int? = null,
    val portfolioAvgPriceLine: String? = null,
    val orderBook: SecurityOrderBookDto? = null,
)
