package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.HoldingsEnvelopeDto
import com.zovdeneg.app.data.remote.dto.PortfolioSummaryDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import javax.inject.Inject

internal class ZovPortfolioApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getPortfolioSummary(): PortfolioSummaryDto =
        client.get(ZovApiPaths.PORTFOLIO_SUMMARY).body()

    suspend fun getHoldingsEnvelope(): HoldingsEnvelopeDto =
        client.get(ZovApiPaths.PORTFOLIO_HOLDINGS).body()
}
