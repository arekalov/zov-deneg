package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class TransactionDto(
    val title: String,
    val date: String,
    val amountText: String,
    val side: String,
)

@Serializable
internal data class TransactionsEnvelopeDto(
    val transactions: List<TransactionDto>,
)
