package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.ZovJson
import com.zovdeneg.app.data.remote.api.ZovOrdersApi
import com.zovdeneg.app.data.remote.dto.OrderResponseDto
import com.zovdeneg.app.data.remote.dto.ZovApiErrorBody
import com.zovdeneg.app.domain.orders.InsufficientFundsForOrderException
import com.zovdeneg.app.domain.orders.InsufficientSecuritiesForOrderException
import com.zovdeneg.app.domain.orders.OrderReceipt
import com.zovdeneg.app.domain.orders.OrdersRepository
import com.zovdeneg.app.domain.orders.UserOrder

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.statement.bodyAsText

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
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { e ->
                when (e) {
                    is ClientRequestException -> mapPlaceMarketBuyClientException(e)
                    else -> Result.failure(e)
                }
            },
        )

    private suspend fun mapPlaceMarketBuyClientException(e: ClientRequestException): Result<OrderReceipt> {
        val bodyText = runCatching { e.response.bodyAsText() }.getOrNull().orEmpty()
        val parsed =
            runCatching { ZovJson.decodeFromString(ZovApiErrorBody.serializer(), bodyText) }.getOrNull()
        return when (parsed?.code) {
            "INSUFFICIENT_FUNDS" ->
                Result.failure(
                    InsufficientFundsForOrderException(
                        parsed.message?.takeIf { it.isNotBlank() },
                    ),
                )
            else -> Result.failure(e)
        }
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
