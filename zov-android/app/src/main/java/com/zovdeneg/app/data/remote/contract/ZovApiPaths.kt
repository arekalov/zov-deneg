package com.zovdeneg.app.data.remote.contract

object ZovApiPaths {
    const val PORTFOLIO_SUMMARY = "/v1/portfolio/summary"
    const val PORTFOLIO_HOLDINGS = "/v1/portfolio/holdings"
    const val SECURITIES_POPULAR = "/v1/securities/popular"
    const val TRANSACTIONS = "/v1/transactions"
    const val ORDERS = "/v1/orders"
    const val BALANCE = "/v1/balance"
    const val BALANCE_DEPOSIT = "/v1/balance/deposit"
    const val BALANCE_WITHDRAW = "/v1/balance/withdraw"
    const val USERS_ME = "/v1/users/me"
    const val USERS_ME_PIN = "/v1/users/me/pin"
    const val AUTH_LOGIN = "/v1/auth/login"
    const val AUTH_REGISTER = "/v1/auth/register"

    fun securityDetail(ticker: String): String = "/v1/securities/${ticker.trim('/')}"
}
