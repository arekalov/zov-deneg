package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.api.ZovOrdersApi
import com.zovdeneg.app.domain.orders.OrderReceipt
import com.zovdeneg.app.domain.orders.OrdersRepository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class OrdersRepositoryImpl @Inject constructor(
    private val ordersApi: ZovOrdersApi,
) : OrdersRepository {
    override suspend fun placeMarketBuy(securityId: String, quantity: Int): Result<OrderReceipt> =
        runCatching {
            val dto = ordersApi.createMarketBuy(securityId, quantity)
            OrderReceipt(
                status = dto.status,
                totalAmountText = dto.totalAmount,
            )
        }
}
