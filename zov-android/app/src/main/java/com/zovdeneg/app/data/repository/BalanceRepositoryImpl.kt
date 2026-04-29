package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.format.ZovRubDisplay
import com.zovdeneg.app.data.remote.api.ZovBalanceApi
import com.zovdeneg.app.data.remote.dto.BalanceDto
import com.zovdeneg.app.domain.balance.BalanceRepository
import com.zovdeneg.app.domain.balance.BrokerageBalance

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class BalanceRepositoryImpl @Inject constructor(
    private val balanceApi: ZovBalanceApi,
) : BalanceRepository {
    override suspend fun getBalance(): Result<BrokerageBalance> =
        runCatching { balanceApi.getBalance().toDomain() }

    override suspend fun depositDecimalString(amount: String): Result<BrokerageBalance> =
        runCatching { balanceApi.deposit(amount).toDomain() }

    override suspend fun withdrawDecimalString(amount: String): Result<BrokerageBalance> =
        runCatching { balanceApi.withdraw(amount).toDomain() }

    private fun BalanceDto.toDomain(): BrokerageBalance =
        BrokerageBalance(
            availableText = available.toRubDisplay(),
            blockedText = blocked.toRubDisplay(),
            totalText = total.toRubDisplay(),
            availableDecimal = available.trim(),
        )

    private fun String.toRubDisplay(): String = ZovRubDisplay.formatApiDecimalToRubLine(this)
}
