package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.format.ZovRubDisplay
import com.zovdeneg.app.data.remote.api.ZovSecuritiesApi
import com.zovdeneg.app.data.remote.dto.SecurityCardRemoteDto
import com.zovdeneg.app.data.remote.dto.SecurityDetailDto
import com.zovdeneg.app.data.remote.dto.toSecurityOrderBookDto
import com.zovdeneg.app.data.remote.dto.SecurityOrderBookDto
import com.zovdeneg.app.data.remote.dto.SecurityOrderBookLevelDto
import com.zovdeneg.app.data.remote.dto.SecurityPriceHistoryDto
import com.zovdeneg.app.domain.market.PriceHistoryPoint
import com.zovdeneg.app.domain.market.SecuritiesRepository
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.market.SecurityOrderBook
import com.zovdeneg.app.domain.market.SecurityOrderBookRow
import com.zovdeneg.app.domain.PageEnvelope
import com.zovdeneg.app.domain.market.SecurityKind
import com.zovdeneg.app.domain.market.SecurityListItem
import com.zovdeneg.app.domain.market.SecurityPriceHistory

import java.util.Locale

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class SecuritiesRepositoryImpl @Inject constructor(
    private val securitiesApi: ZovSecuritiesApi,
) : SecuritiesRepository {
    override suspend fun getSecuritiesPage(
        query: String,
        type: String?,
        page: Int,
        pageSize: Int,
    ): Result<PageEnvelope<SecurityListItem>> =
        runCatching {
            val dto = securitiesApi.getSecuritiesPage(query, type, page, pageSize)
            PageEnvelope(
                items = dto.data.map { it.toListItem() },
                page = dto.pagination.page,
                pageSize = dto.pagination.pageSize,
                totalPages = dto.pagination.totalPages,
            )
        }

    override suspend fun getSecurityDetail(ticker: String): Result<SecurityDetail> =
        runCatching {
            securitiesApi.getSecurityDetail(ticker).toDomain()
        }

    override suspend fun getSecurityOrderBook(navId: String): Result<SecurityOrderBook> =
        runCatching {
            securitiesApi.getSecurityOrderBook(navId).toSecurityOrderBookDto().toDomain()
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
            priceLine = ZovRubDisplay.formatApiDecimalToRubLine(priceLine.removeSuffix("₽").trim()),
            changeLine = formatDetailChangeLine(changeLine),
            changePositive = changePositive,
            securityId = securityId,
            lotSize = lotSize,
            orderBookText = orderBookText,
            sectorName = sectorName,
            exchangeCode = exchangeCode,
            companyDescription = companyDescription,
            portfolioQuantity = portfolioQuantity,
            portfolioAvgPriceLine = portfolioAvgPriceLine?.takeIf { it.isNotBlank() }?.let { line ->
                ZovRubDisplay.formatApiDecimalToRubLine(line.removeSuffix("₽").trim())
            },
            orderBook = orderBook?.toDomain(),
        )

    private fun formatDetailChangeLine(raw: String): String {
        val sep = " · "
        if (!raw.contains(sep)) return raw
        val idx = raw.indexOf(sep)
        val left = raw.substring(0, idx)
        val right = raw.substring(idx + sep.length)
        val rubPart = right.trim().removeSuffix("₽").trim()
        val formattedRub =
            runCatching { ZovRubDisplay.formatSignedDecimalRubLine(rubPart) }.getOrElse { right }
        val formattedLeft =
            if (left.trim().endsWith("%")) {
                ZovRubDisplay.formatPercentTwoDecimals(left.trim().removeSuffix("%"))
            } else {
                left
            }
        return "$formattedLeft · $formattedRub"
    }

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

    private fun SecurityCardRemoteDto.toListItem(): SecurityListItem =
        SecurityListItem(
            ticker = ticker,
            subtitle = name,
            valueText = ZovRubDisplay.formatApiDecimalToRubLine(lastPrice.trim()),
            deltaText = ZovRubDisplay.formatPercentTwoDecimals(priceChangePct.trim()),
            deltaPositive = !priceChange.trim().startsWith("-"),
            kind = type.toSecurityKind(),
            detailNavKey = id,
        )

    private fun String.toSecurityKind(): SecurityKind =
        when (lowercase(Locale.ROOT)) {
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
