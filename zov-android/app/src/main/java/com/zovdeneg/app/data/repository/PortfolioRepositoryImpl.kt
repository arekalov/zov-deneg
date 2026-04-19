package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.api.ZovPortfolioApi
import com.zovdeneg.app.data.remote.dto.HoldingDto
import com.zovdeneg.app.data.remote.dto.PortfolioSummaryDto
import com.zovdeneg.app.domain.portfolio.Holding
import com.zovdeneg.app.domain.portfolio.PortfolioRepository
import com.zovdeneg.app.domain.portfolio.PortfolioSummary
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class PortfolioRepositoryImpl @Inject constructor(
    private val portfolioApi: ZovPortfolioApi,
) : PortfolioRepository {
    override suspend fun refreshPortfolioSummary(): Result<PortfolioSummary> =
        runCatching { portfolioApi.getPortfolioSummary().toDomain() }

    override suspend fun refreshHoldings(): Result<List<Holding>> =
        runCatching {
            portfolioApi.getHoldingsEnvelope().holdings.map { it.toDomain() }
        }

    private fun HoldingDto.toDomain(): Holding =
        Holding(
            ticker = ticker,
            subtitle = subtitle,
            valueText = valueText,
            deltaText = deltaText,
            deltaPositive = deltaPositive,
        )

    private fun PortfolioSummaryDto.toDomain(): PortfolioSummary =
        PortfolioSummary(
            portfolioAmountRub = portfolioAmountRub,
            totalGainText = totalGainText,
        )
}
