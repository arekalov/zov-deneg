package com.zovdeneg.app.domain.market

interface SecuritiesRepository {
    suspend fun getPopularSecurities(): Result<List<SecurityListItem>>

    suspend fun getSecurityDetail(ticker: String): Result<SecurityDetail>

    suspend fun getSecurityOrderBook(navId: String): Result<SecurityOrderBook>

    suspend fun getSecurityPriceHistory(
        ticker: String,
        fromEpochSeconds: Long,
        toEpochSeconds: Long,
    ): Result<SecurityPriceHistory>
}
