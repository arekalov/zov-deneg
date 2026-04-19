package com.zovdeneg.app.domain.portfolio

interface PortfolioRepository {
    suspend fun refreshPortfolioSummary(): Result<PortfolioSummary>

    suspend fun refreshHoldings(): Result<List<Holding>>
}
