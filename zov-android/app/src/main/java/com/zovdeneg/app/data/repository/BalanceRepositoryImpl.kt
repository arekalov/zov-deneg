package com.zovdeneg.app.data.repository

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
        )

    private fun String.toRubDisplay(): String {
        val trimmed = trim()
        val sign = if (trimmed.startsWith("-")) "−" else ""
        val n = trimmed.removePrefix("-")
        val parts = n.split(".")
        val intRaw = parts[0].ifEmpty { "0" }
        val frac = parts.getOrNull(1)?.take(2)?.padEnd(2, '0') ?: "00"
        val grouped = intRaw.reversed().chunked(3).joinToString(" ").reversed()
        return "$sign$grouped,$frac ₽"
    }
}
