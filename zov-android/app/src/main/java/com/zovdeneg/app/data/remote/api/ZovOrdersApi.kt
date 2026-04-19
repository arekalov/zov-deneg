package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.CreateOrderRequestDto
import com.zovdeneg.app.data.remote.dto.OrderResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody

import javax.inject.Inject

internal class ZovOrdersApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun createMarketBuy(securityId: String, quantity: Int): OrderResponseDto =
        client.post(ZovApiPaths.ORDERS) {
            setBody(
                CreateOrderRequestDto(
                    securityId = securityId,
                    side = "buy",
                    quantity = quantity,
                ),
            )
        }.body()
}
