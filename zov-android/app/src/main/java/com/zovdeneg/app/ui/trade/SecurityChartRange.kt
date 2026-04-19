package com.zovdeneg.app.ui.trade

enum class SecurityChartRange {
    ONE_DAY,
    ONE_WEEK,
    ONE_MONTH,
    ONE_YEAR,
}

fun SecurityChartRange.rangeSeconds(): Long =
    when (this) {
        SecurityChartRange.ONE_DAY -> 86400L
        SecurityChartRange.ONE_WEEK -> 7L * 86400
        SecurityChartRange.ONE_MONTH -> 30L * 86400
        SecurityChartRange.ONE_YEAR -> 365L * 86400
    }
