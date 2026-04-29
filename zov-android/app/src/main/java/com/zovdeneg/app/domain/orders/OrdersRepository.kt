package com.zovdeneg.app.domain.orders

interface OrdersRepository {
    suspend fun placeMarketBuy(securityId: String, quantity: Int): Result<OrderReceipt>

    suspend fun placeMarketSell(securityId: String, quantity: Int): Result<OrderReceipt>

    suspend fun listOrders(): Result<List<UserOrder>>

    suspend fun getOrder(orderId: String): Result<UserOrder>

    suspend fun cancelOrder(orderId: String): Result<Unit>
}
