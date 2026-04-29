package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.CreateOrderRequestDto
import com.zovdeneg.app.data.remote.dto.OrderResponseDto
import com.zovdeneg.app.data.remote.dto.OrdersListRemoteDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpStatusCode

import javax.inject.Inject

internal class ZovOrdersApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun createMarketBuy(securityId: String, quantity: Int): OrderResponseDto =
        createMarketOrder(securityId, side = "buy", quantity = quantity)

    suspend fun createMarketSell(securityId: String, quantity: Int): OrderResponseDto =
        createMarketOrder(securityId, side = "sell", quantity = quantity)

    private suspend fun createMarketOrder(
        securityId: String,
        side: String,
        quantity: Int,
    ): OrderResponseDto =
        client.post(ZovApiPaths.ORDERS) {
            setBody(
                CreateOrderRequestDto(
                    securityId = securityId,
                    side = side,
                    quantity = quantity,
                ),
            )
        }.body()

    suspend fun getOrdersList(): OrdersListRemoteDto =
        client.get(ZovApiPaths.ORDERS) {
            parameter("page", 1)
            parameter("pageSize", 50)
        }.body()

    suspend fun getOrder(orderId: String): OrderResponseDto =
        client.get(ZovApiPaths.order(orderId)).body()

    suspend fun cancelOrder(orderId: String) {
        val response = client.delete(ZovApiPaths.order(orderId))
        if (response.status != HttpStatusCode.NoContent && response.status != HttpStatusCode.OK) {
            error("cancel_order_http_${response.status.value}")
        }
    }
}
