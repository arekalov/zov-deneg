package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.PopularSecuritiesEnvelopeDto
import com.zovdeneg.app.data.remote.dto.SecurityDetailDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

import javax.inject.Inject

internal class ZovSecuritiesApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getPopularEnvelope(): PopularSecuritiesEnvelopeDto =
        client.get(ZovApiPaths.SECURITIES_POPULAR).body()

    suspend fun getSecurityDetail(ticker: String): SecurityDetailDto =
        client.get(ZovApiPaths.securityDetail(ticker)).body()
}
