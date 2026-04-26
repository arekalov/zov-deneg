package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.format.ZovRubDisplay
import com.zovdeneg.app.data.remote.ZovJson
import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.OrderBookRemoteDto
import com.zovdeneg.app.data.remote.dto.PopularSecuritiesEnvelopeDto
import com.zovdeneg.app.data.remote.dto.PopularSecurityDto
import com.zovdeneg.app.data.remote.dto.SecuritiesListRemoteDto
import com.zovdeneg.app.data.remote.dto.SecurityCardRemoteDto
import com.zovdeneg.app.data.remote.dto.SecurityDetailDto
import com.zovdeneg.app.data.remote.dto.SecurityPriceHistoryDto
import com.zovdeneg.app.data.remote.dto.toSecurityDetailDto
import com.zovdeneg.app.di.ZovSecuritiesHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

internal class ZovSecuritiesApi @Inject constructor(
    @param:ZovSecuritiesHttpClient private val client: HttpClient,
) {
    suspend fun getPopularEnvelope(): PopularSecuritiesEnvelopeDto {
        val list: SecuritiesListRemoteDto =
            client.get(ZovApiPaths.SECURITIES_LIST) {
                parameter("pageSize", 50)
                parameter("page", 1)
            }.body()
        val items =
            list.data.map { s ->
                PopularSecurityDto(
                    ticker = s.ticker,
                    subtitle = s.name,
                    valueText = ZovRubDisplay.formatApiDecimalToRubLine(s.lastPrice.trim()),
                    deltaText = ZovRubDisplay.formatPercentTwoDecimals(s.priceChangePct.trim()),
                    deltaPositive = !s.priceChange.trim().startsWith("-"),
                    kind = s.type,
                    securityId = s.id,
                )
            }
        return PopularSecuritiesEnvelopeDto(items = items)
    }

    suspend fun getSecurityDetail(securityNavId: String): SecurityDetailDto {
        val raw: String = client.get(ZovApiPaths.securityDetail(securityNavId)).body<String>()
        val asAppDto = runCatching { ZovJson.decodeFromString(SecurityDetailDto.serializer(), raw) }
        if (asAppDto.isSuccess) {
            return asAppDto.getOrThrow()
        }
        val card = ZovJson.decodeFromString(SecurityCardRemoteDto.serializer(), raw)
        return card.toSecurityDetailDto(orderBook = null)
    }

    suspend fun getSecurityOrderBook(securityNavId: String): OrderBookRemoteDto {
        val obRaw: String = client.get(ZovApiPaths.securityOrderBook(securityNavId)).body()
        return ZovJson.decodeFromString(OrderBookRemoteDto.serializer(), obRaw)
    }

    suspend fun getSecurityPriceHistory(
        securityNavId: String,
        fromEpochSeconds: Long,
        toEpochSeconds: Long,
    ): SecurityPriceHistoryDto =
        client.get(ZovApiPaths.securityPriceHistory(securityNavId)) {
            parameter("from", fromEpochSeconds)
            parameter("to", toEpochSeconds)
        }.body()
}
