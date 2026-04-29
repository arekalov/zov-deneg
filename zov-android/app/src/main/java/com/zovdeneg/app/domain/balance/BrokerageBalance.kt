package com.zovdeneg.app.domain.balance

data class BrokerageBalance(
    val availableText: String,
    val blockedText: String,
    val totalText: String,
    /** Десятичная строка API для «доступно» (точка как разделитель дроби), для оценок в UI. */
    val availableDecimal: String = "",
)
