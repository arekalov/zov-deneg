package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.PopularSecuritiesEnvelopeDto
import com.zovdeneg.app.data.remote.dto.SecurityDetailDto
import com.zovdeneg.app.data.remote.dto.SecurityPriceHistoryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

import javax.inject.Inject

internal class ZovSecuritiesApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getPopularEnvelope(): PopularSecuritiesEnvelopeDto =
        client.get(ZovApiPaths.SECURITIES_POPULAR).body()

    suspend fun getSecurityDetail(ticker: String): SecurityDetailDto =
        client.get(ZovApiPaths.securityDetail(ticker)).body()

    suspend fun getSecurityPriceHistory(
        ticker: String,
        fromEpochSeconds: Long,
        toEpochSeconds: Long,
    ): SecurityPriceHistoryDto =
        client.get(ZovApiPaths.securityPriceHistory(ticker)) {
            parameter("from", fromEpochSeconds)
            parameter("to", toEpochSeconds)
        }.body()
}
