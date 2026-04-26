package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.format.ZovRubDisplay
import com.zovdeneg.app.data.remote.api.ZovPortfolioApi
import com.zovdeneg.app.data.remote.dto.PortfolioItemRemoteDto
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
            portfolioApi.getPortfolio().items.map { it.toDomain() }
        }

    private fun PortfolioItemRemoteDto.toDomain(): Holding =
        Holding(
            ticker = security.ticker,
            subtitle = "${security.name} · $quantity шт.",
            valueText = ZovRubDisplay.formatApiDecimalToRubLine(currentValue.trim()),
            deltaText = "${ZovRubDisplay.formatSignedDecimalRubLine(profitLoss.trim())} (${profitLossPct.trim()}%)",
            deltaPositive = !profitLoss.trim().startsWith("-"),
            detailNavKey = securityId,
        )

    private fun PortfolioSummaryDto.toDomain(): PortfolioSummary =
        PortfolioSummary(
            portfolioAmountRub = ZovRubDisplay.formatApiDecimalToRubLine(totalValue.trim()),
            totalGainText = "${ZovRubDisplay.formatSignedDecimalRubLine(profitLoss.trim())} (${profitLossPct.trim()}%)",
        )
}
