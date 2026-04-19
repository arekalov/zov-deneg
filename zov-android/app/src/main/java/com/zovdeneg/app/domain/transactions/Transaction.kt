package com.zovdeneg.app.domain.transactions

data class Transaction(
    val title: String,
    val date: String,
    val amountText: String,
    val side: TransactionSide,
)

enum class TransactionSide {
    PURCHASE,
    SALE,
    OTHER,
}
