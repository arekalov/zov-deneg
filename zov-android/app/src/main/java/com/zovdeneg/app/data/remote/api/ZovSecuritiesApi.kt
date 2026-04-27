package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.ZovJson
import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.OrderBookRemoteDto
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
    suspend fun getSecuritiesPage(
        query: String,
        type: String?,
        page: Int,
        pageSize: Int,
    ): SecuritiesListRemoteDto =
        client.get(ZovApiPaths.SECURITIES_LIST) {
            if (query.isNotBlank()) {
                parameter("q", query)
            }
            if (!type.isNullOrBlank()) {
                parameter("type", type)
            }
            parameter("page", page)
            parameter("pageSize", pageSize)
        }.body()

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
