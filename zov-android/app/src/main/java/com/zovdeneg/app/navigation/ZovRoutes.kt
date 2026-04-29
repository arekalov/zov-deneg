package com.zovdeneg.app.navigation

object ZovRoutes {
    const val LOGIN = "login"
    const val REGISTER_FLOW = "register_flow"
    const val REGISTER_STEP1 = "register_step1"
    const val REGISTER_STEP2 = "register_step2"

    /** Маршрут composable шага PIN (с аргументом для сброса черновика после входа по паролю). */
    const val REGISTER_STEP2_ROUTE = "register_step2?freshPin={freshPin}"
    const val REGISTER_STEP3 = "register_step3"
    const val REGISTER_STEP4 = "register_step4"
    const val MAIN_HOME = "main_home"
    const val MAIN_SEARCH = "main_search"
    const val MAIN_HISTORY = "main_history"
    const val PROFILE = "profile"
    const val EDIT_PROFILE = "edit_profile"
    const val CHANGE_PIN = "change_pin"
    const val DEPOSIT = "deposit"

    /** Список заявок (порядок composable в графе: сначала деталь `orders/{id}`, затем список `orders`). */
    const val ORDERS_LIST = "orders"
    const val ORDER_DETAIL = "orders/{orderId}"
    const val DETAIL = "detail/{securityId}/{displayTicker}"
    const val BUY = "buy/{securityId}/{displayTicker}"
    const val SELL = "sell/{securityId}/{displayTicker}"

    fun detail(securityId: String, displayTicker: String) =
        "detail/${securityId.encodeTicker()}/${displayTicker.encodeTicker()}"

    fun buy(securityId: String, displayTicker: String) =
        "buy/${securityId.encodeTicker()}/${displayTicker.encodeTicker()}"

    fun sell(securityId: String, displayTicker: String) =
        "sell/${securityId.encodeTicker()}/${displayTicker.encodeTicker()}"

    fun orderDetail(orderId: String) = "$ORDERS_LIST/${orderId.encodeTicker()}"

    /**
     * Шаг PIN внутри графа [REGISTER_FLOW] (не полный путь от корня NavHost).
     * После [navigate][androidx.navigation.NavController.navigate](REGISTER_FLOW) вызывать этот маршрут
     * для сценария «вошли по паролю, локального PIN ещё нет».
     */
    fun registerStep2WithFreshPin(freshPin: Boolean) = "$REGISTER_STEP2?freshPin=$freshPin"

    private fun String.encodeTicker() = replace("/", "_")
}
