package com.zovdeneg.app.navigation

import androidx.navigation.NavHostController

/** После пополнения, вывода или сделки — обновить главную, историю и профиль (если записи есть в стеке). */
internal fun NavHostController.notifyPortfolioOrBalanceChangedExternally() {
    listOf(
        ZovRoutes.MAIN_HOME to MAIN_HOME_DATA_REFRESH_TICK_KEY,
        ZovRoutes.MAIN_HISTORY to MAIN_HISTORY_DATA_REFRESH_TICK_KEY,
    ).forEach { (route, key) ->
        runCatching {
            getBackStackEntry(route).savedStateHandle[key] = System.nanoTime()
        }
    }
    runCatching {
        getBackStackEntry(ZovRoutes.PROFILE).savedStateHandle[PROFILE_REFRESH_TICK_KEY] =
            System.nanoTime()
    }
}
