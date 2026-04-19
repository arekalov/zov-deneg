package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.api.ZovSecuritiesApi
import com.zovdeneg.app.data.remote.dto.PopularSecurityDto
import com.zovdeneg.app.data.remote.dto.SecurityDetailDto
import com.zovdeneg.app.data.remote.dto.SecurityOrderBookDto
import com.zovdeneg.app.data.remote.dto.SecurityOrderBookLevelDto
import com.zovdeneg.app.data.remote.dto.SecurityPriceHistoryDto
import com.zovdeneg.app.domain.market.PriceHistoryPoint
import com.zovdeneg.app.domain.market.SecuritiesRepository
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.market.SecurityOrderBook
import com.zovdeneg.app.domain.market.SecurityOrderBookRow
import com.zovdeneg.app.domain.market.SecurityKind
import com.zovdeneg.app.domain.market.SecurityListItem
import com.zovdeneg.app.domain.market.SecurityPriceHistory

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SecuritiesRepositoryImpl @Inject constructor(
    private val securitiesApi: ZovSecuritiesApi,
) : SecuritiesRepository {
    override suspend fun getPopularSecurities(): Result<List<SecurityListItem>> =
        runCatching {
            securitiesApi.getPopularEnvelope().items.map { it.toDomain() }
        }

    override suspend fun getSecurityDetail(ticker: String): Result<SecurityDetail> =
        runCatching {
            securitiesApi.getSecurityDetail(ticker).toDomain()
        }

    override suspend fun getSecurityPriceHistory(
        ticker: String,
        fromEpochSeconds: Long,
        toEpochSeconds: Long,
    ): Result<SecurityPriceHistory> =
        runCatching {
            securitiesApi.getSecurityPriceHistory(ticker, fromEpochSeconds, toEpochSeconds).toDomain()
        }

    private fun SecurityDetailDto.toDomain(): SecurityDetail =
        SecurityDetail(
            ticker = ticker,
            subtitle = subtitle,
            priceLine = priceLine,
            changeLine = changeLine,
            changePositive = changePositive,
            securityId = securityId,
            lotSize = lotSize,
            orderBookText = orderBookText,
            sectorName = sectorName,
            exchangeCode = exchangeCode,
            companyDescription = companyDescription,
            portfolioQuantity = portfolioQuantity,
            portfolioAvgPriceLine = portfolioAvgPriceLine,
            orderBook = orderBook?.toDomain(),
        )

    private fun SecurityOrderBookDto.toDomain(): SecurityOrderBook =
        SecurityOrderBook(
            askLevels = askLevels.map { it.toDomain() },
            bestAskPriceDecimal = bestAskPrice,
            bidLevels = bidLevels.map { it.toDomain() },
            bestBidPriceDecimal = bestBidPrice,
        )

    private fun SecurityOrderBookLevelDto.toDomain(): SecurityOrderBookRow =
        SecurityOrderBookRow(
            leftQuantity = quantity,
            priceDecimal = price,
            rightQuantity = mirrorQuantity,
        )

    private fun PopularSecurityDto.toDomain(): SecurityListItem =
        SecurityListItem(
            ticker = ticker,
            subtitle = subtitle,
            valueText = valueText,
            deltaText = deltaText,
            deltaPositive = deltaPositive,
            kind = kind.toSecurityKind(),
        )

    private fun String.toSecurityKind(): SecurityKind =
        when (lowercase()) {
            "bond" -> SecurityKind.BOND
            "etf" -> SecurityKind.ETF
            else -> SecurityKind.STOCK
        }

    private fun SecurityPriceHistoryDto.toDomain(): SecurityPriceHistory {
        val sortedPoints =
            data.map { PriceHistoryPoint(it.timestamp, parseDecimalString(it.price)) }
                .sortedBy { it.timestampSeconds }
        return SecurityPriceHistory(
            securityId = securityId,
            ticker = ticker,
            fromSeconds = from,
            toSeconds = to,
            points = sortedPoints,
        )
    }

    private fun parseDecimalString(raw: String): Double =
        raw.trim().replace(',', '.').toDouble()
}
