package com.zovdeneg.app.domain.transactions

fun interface TransactionsRepository {
    suspend fun getTransactions(): Result<List<Transaction>>
}
