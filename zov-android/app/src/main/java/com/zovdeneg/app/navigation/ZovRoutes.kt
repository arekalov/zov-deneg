package com.zovdeneg.app.navigation

object ZovRoutes {
    const val LOGIN = "login"
    const val REGISTER_STEP1 = "register_step1"
    const val REGISTER_STEP2 = "register_step2"
    const val REGISTER_STEP3 = "register_step3"
    const val REGISTER_STEP4 = "register_step4"
    const val MAIN_HOME = "main_home"
    const val MAIN_SEARCH = "main_search"
    const val MAIN_HISTORY = "main_history"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PIN = "change_pin"
    const val DEPOSIT = "deposit"
    const val DETAIL = "detail/{ticker}"
    const val BUY = "buy/{ticker}"

    fun detail(ticker: String) = "detail/${ticker.encodeTicker()}"

    fun buy(ticker: String) = "buy/${ticker.encodeTicker()}"

    private fun String.encodeTicker() = replace("/", "_")
}
