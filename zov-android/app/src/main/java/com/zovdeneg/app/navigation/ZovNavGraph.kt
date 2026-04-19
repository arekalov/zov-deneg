package com.zovdeneg.app.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.zovdeneg.app.ui.screens.BuyScreen
import com.zovdeneg.app.ui.screens.ChangePinScreen
import com.zovdeneg.app.ui.screens.DepositScreen
import com.zovdeneg.app.ui.screens.EditProfileScreen
import com.zovdeneg.app.ui.screens.HistoryTabScreen
import com.zovdeneg.app.ui.screens.LoginScreen
import com.zovdeneg.app.ui.screens.MainHomeScreen
import com.zovdeneg.app.ui.screens.ProfileScreen
import com.zovdeneg.app.ui.screens.RegisterBiometricScreen
import com.zovdeneg.app.ui.screens.RegisterDataScreen
import com.zovdeneg.app.ui.screens.RegisterPinConfirmScreen
import com.zovdeneg.app.ui.screens.RegisterPinScreen
import com.zovdeneg.app.ui.screens.SearchTabScreen
import com.zovdeneg.app.ui.screens.SecurityDetailScreen

internal class ZovNavGraph private constructor() {
    companion object {
        const val BOTTOM_HOME = "Home"
        const val BOTTOM_SEARCH = "Search"
        const val BOTTOM_HISTORY = "History"

        const val CD_BACK = "Back"
        const val CD_PROFILE = "Profile"

        const val TOP_HOME = "Home"
        const val TOP_SEARCH = "Search securities"
        const val TOP_HISTORY = "Transaction history"
        const val TOP_PROFILE = "Profile and settings"
        const val TOP_EDIT_PROFILE = "Edit profile"
        const val TOP_CHANGE_PIN = "Change PIN"
        const val TOP_DEPOSIT = "Brokerage account"
        const val TOP_SIGN_UP = "Sign up"

        private const val TOP_DETAIL_PREFIX = "Details · "
        private const val TOP_BUY_PREFIX = "Buy "

        fun topBarDetail(ticker: String): String = TOP_DETAIL_PREFIX + ticker

        fun topBarBuy(ticker: String): String = TOP_BUY_PREFIX + ticker
    }
}

@Composable
internal fun ZovNavGraphHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ZovRoutes.LOGIN,
        modifier = modifier.fillMaxSize(),
    ) {
        zovAuthDestinations(navController)
        zovTabDestinations(navController)
        zovProfileAndTradeDestinations(navController)
    }
}

private fun NavGraphBuilder.zovAuthDestinations(navController: NavHostController) {
    composable(ZovRoutes.LOGIN) {
        LoginScreen(
            onLoggedIn = {
                navController.navigate(ZovRoutes.MAIN_HOME) {
                    popUpTo(ZovRoutes.LOGIN) { inclusive = true }
                }
            },
            onRegister = { navController.navigate(ZovRoutes.REGISTER_STEP1) },
        )
    }
    composable(ZovRoutes.REGISTER_STEP1) {
        RegisterDataScreen(
            onNext = { navController.navigate(ZovRoutes.REGISTER_STEP2) },
            onLogin = { navController.popBackStack(ZovRoutes.LOGIN, false) },
        )
    }
    composable(ZovRoutes.REGISTER_STEP2) {
        RegisterPinScreen(onNext = { navController.navigate(ZovRoutes.REGISTER_STEP3) })
    }
    composable(ZovRoutes.REGISTER_STEP3) {
        RegisterPinConfirmScreen(onNext = { navController.navigate(ZovRoutes.REGISTER_STEP4) })
    }
    composable(ZovRoutes.REGISTER_STEP4) {
        RegisterBiometricScreen(
            onDone = {
                navController.navigate(ZovRoutes.MAIN_HOME) {
                    popUpTo(ZovRoutes.LOGIN) { inclusive = true }
                }
            },
        )
    }
}

private fun NavGraphBuilder.zovTabDestinations(navController: NavHostController) {
    composable(ZovRoutes.MAIN_HOME) {
        MainHomeScreen(
            onOpenBrokerAccount = { navController.navigate(ZovRoutes.DEPOSIT) },
            onOpenSecurity = { ticker ->
                navController.navigate(ZovRoutes.detail(ticker))
            },
        )
    }
    composable(ZovRoutes.MAIN_SEARCH) {
        SearchTabScreen(
            onOpenSecurity = { ticker ->
                navController.navigate(ZovRoutes.detail(ticker))
            },
        )
    }
    composable(ZovRoutes.MAIN_HISTORY) { HistoryTabScreen() }
}

private fun NavGraphBuilder.zovProfileAndTradeDestinations(navController: NavHostController) {
    composable(ZovRoutes.PROFILE) {
        ProfileScreen(
            onEditProfile = { navController.navigate(ZovRoutes.EDIT_PROFILE) },
            onChangePin = { navController.navigate(ZovRoutes.CHANGE_PIN) },
            onLogout = {
                navController.navigate(ZovRoutes.LOGIN) {
                    popUpTo(navController.graph.id) { inclusive = true }
                }
            },
        )
    }
    composable(ZovRoutes.EDIT_PROFILE) {
        EditProfileScreen(onBack = { navController.popBackStack() })
    }
    composable(ZovRoutes.CHANGE_PIN) {
        ChangePinScreen(onBack = { navController.popBackStack() })
    }
    composable(ZovRoutes.DEPOSIT) {
        DepositScreen(onBack = { navController.popBackStack() })
    }
    composable(
        ZovRoutes.DETAIL,
        arguments = listOf(navArgument("ticker") { type = NavType.StringType }),
    ) { entry ->
        val ticker = entry.arguments?.getString("ticker").orEmpty()
        SecurityDetailScreen(
            ticker = ticker,
            onBuy = { navController.navigate(ZovRoutes.buy(ticker)) },
        )
    }
    composable(
        ZovRoutes.BUY,
        arguments = listOf(navArgument("ticker") { type = NavType.StringType }),
    ) { entry ->
        val ticker = entry.arguments?.getString("ticker").orEmpty()
        BuyScreen(ticker = ticker, onBack = { navController.popBackStack() })
    }
}
