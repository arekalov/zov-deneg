package com.zovdeneg.app.data.remote.contract

object ZovApiPaths {
    const val PORTFOLIO = "/portfolio"
    const val PORTFOLIO_SUMMARY = "/portfolio/summary"
    const val SECURITIES_LIST = "/securities"
    const val TRANSACTIONS = "/transactions"
    const val ORDERS = "/orders"
    const val BALANCE = "/balance"
    const val BALANCE_DEPOSIT = "/balance/deposit"
    const val BALANCE_WITHDRAW = "/balance/withdraw"
    const val USERS_ME = "/users/me"
    const val USERS_ME_PIN = "/users/me/pin"
    const val AUTH_LOGIN = "/auth/login"
    const val AUTH_REGISTER = "/auth/register"
    const val AUTH_REFRESH = "/auth/token/refresh"

    fun securityDetail(securityId: String): String = "/securities/${securityId.trim('/')}"

    fun securityPriceHistory(securityId: String): String =
        "/securities/${securityId.trim('/')}/price/history"

    fun securityOrderBook(securityId: String): String =
        "/securities/${securityId.trim('/')}/orderbook"
}
