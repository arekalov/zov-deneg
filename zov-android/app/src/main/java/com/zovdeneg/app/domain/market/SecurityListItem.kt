package com.zovdeneg.app.domain.market

data class SecurityListItem(
    val ticker: String,
    val subtitle: String,
    val valueText: String,
    val deltaText: String,
    val deltaPositive: Boolean,
    val kind: SecurityKind,
)

enum class SecurityKind {
    STOCK,
    BOND,
    ETF,
}
