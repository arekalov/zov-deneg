package com.zovdeneg.app.domain.orders

interface OrdersRepository {
    suspend fun placeMarketBuy(securityId: String, quantity: Int): Result<OrderReceipt>
}
