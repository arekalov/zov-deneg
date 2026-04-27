package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.api.ZovOrdersApi
import com.zovdeneg.app.data.remote.dto.OrderResponseDto
import com.zovdeneg.app.domain.orders.OrderReceipt
import com.zovdeneg.app.domain.orders.OrdersRepository
import com.zovdeneg.app.domain.orders.UserOrder

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

    override suspend fun listOrders(): Result<List<UserOrder>> =
        runCatching {
            ordersApi.getOrdersList().data.map { it.toDomain() }
        }

    override suspend fun getOrder(orderId: String): Result<UserOrder> =
        runCatching {
            ordersApi.getOrder(orderId).toDomain()
        }

    override suspend fun cancelOrder(orderId: String): Result<Unit> =
        runCatching {
            ordersApi.cancelOrder(orderId)
        }

    private fun OrderResponseDto.toDomain(): UserOrder =
        UserOrder(
            id = id,
            securityId = securityId,
            ticker = ticker.orEmpty(),
            side = side,
            status = status,
            quantity = quantity,
            executedPrice = executedPrice,
            executedQuantity = executedQuantity,
            totalAmount = totalAmount,
            commission = commission,
            createdAtEpochSeconds = createdAt,
            updatedAtEpochSeconds = updatedAt,
        )
}
