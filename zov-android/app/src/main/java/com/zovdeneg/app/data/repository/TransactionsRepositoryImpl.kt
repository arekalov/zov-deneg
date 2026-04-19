package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.api.ZovTransactionsApi
import com.zovdeneg.app.data.remote.dto.TransactionDto
import com.zovdeneg.app.domain.transactions.Transaction
import com.zovdeneg.app.domain.transactions.TransactionSide
import com.zovdeneg.app.domain.transactions.TransactionsRepository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionsRepositoryImpl @Inject constructor(
    private val transactionsApi: ZovTransactionsApi,
) : TransactionsRepository {
    override suspend fun getTransactions(): Result<List<Transaction>> =
        runCatching {
            transactionsApi.getTransactionsEnvelope().transactions.map { it.toDomain() }
        }

    private fun TransactionDto.toDomain(): Transaction =
        Transaction(
            title = title,
            date = date,
            amountText = amountText,
            side = side.toTransactionSide(),
        )

    private fun String.toTransactionSide(): TransactionSide =
        when (lowercase()) {
            "purchase", "buy" -> TransactionSide.PURCHASE
            "sale", "sell" -> TransactionSide.SALE
            else -> TransactionSide.OTHER
        }
}
