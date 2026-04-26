package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.format.ZovRubDisplay
import com.zovdeneg.app.data.remote.api.ZovTransactionsApi
import com.zovdeneg.app.data.remote.dto.TransactionRemoteDto
import com.zovdeneg.app.domain.transactions.Transaction
import com.zovdeneg.app.domain.transactions.TransactionSide
import com.zovdeneg.app.domain.transactions.TransactionsRepository

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionsRepositoryImpl @Inject constructor(
    private val transactionsApi: ZovTransactionsApi,
) : TransactionsRepository {
    override suspend fun getTransactions(): Result<List<Transaction>> =
        runCatching {
            transactionsApi.getTransactionsList().data.map { it.toDomain() }
        }

    private fun TransactionRemoteDto.toDomain(): Transaction {
        val label =
            when (type.lowercase(Locale.getDefault())) {
                "buy" -> "Покупка"
                "sell" -> "Продажа"
                "deposit" -> "Пополнение"
                "withdrawal" -> "Вывод"
                "dividend" -> "Дивиденды"
                else -> type
            }
        val tickerPart = ticker?.let { " · $it" }.orEmpty()
        val title = "$label$tickerPart"
        val date =
            DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("ru-RU"))
                .format(Instant.ofEpochSecond(createdAt).atZone(ZoneId.systemDefault()))
        return Transaction(
            title = title,
            date = date,
            amountText = ZovRubDisplay.formatSignedDecimalRubLine(amount),
            side = type.toTransactionSide(),
        )
    }

    private fun String.toTransactionSide(): TransactionSide =
        when (lowercase(Locale.getDefault())) {
            "buy" -> TransactionSide.PURCHASE
            "sell" -> TransactionSide.SALE
            "deposit" -> TransactionSide.DEPOSIT
            "withdrawal" -> TransactionSide.WITHDRAWAL
            "dividend" -> TransactionSide.OTHER
            else -> TransactionSide.OTHER
        }
}
