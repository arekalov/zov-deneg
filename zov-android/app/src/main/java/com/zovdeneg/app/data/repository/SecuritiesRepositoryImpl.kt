package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.api.ZovSecuritiesApi
import com.zovdeneg.app.data.remote.dto.PopularSecurityDto
import com.zovdeneg.app.data.remote.dto.SecurityDetailDto
import com.zovdeneg.app.domain.market.SecuritiesRepository
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.market.SecurityKind
import com.zovdeneg.app.domain.market.SecurityListItem
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
}
