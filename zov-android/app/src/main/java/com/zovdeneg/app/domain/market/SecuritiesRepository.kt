package com.zovdeneg.app.domain.market

import com.zovdeneg.app.domain.PageEnvelope

interface SecuritiesRepository {
    suspend fun getSecuritiesPage(
        query: String,
        type: String?,
        page: Int,
        pageSize: Int,
    ): Result<PageEnvelope<SecurityListItem>>

    suspend fun getSecurityDetail(ticker: String): Result<SecurityDetail>

    suspend fun getSecurityOrderBook(navId: String): Result<SecurityOrderBook>

    suspend fun getSecurityPriceHistory(
        ticker: String,
        fromEpochSeconds: Long,
        toEpochSeconds: Long,
    ): Result<SecurityPriceHistory>
}
