package com.zovdeneg.app.domain.balance

interface BalanceRepository {
    suspend fun getBalance(): Result<BrokerageBalance>

    suspend fun depositDecimalString(amount: String): Result<BrokerageBalance>

    suspend fun withdrawDecimalString(amount: String): Result<BrokerageBalance>
}
