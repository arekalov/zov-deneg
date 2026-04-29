package com.zovdeneg.app.navigation

import com.zovdeneg.app.ui.deposit.DepositViewModel
import com.zovdeneg.app.ui.screens.BuyScreen
import com.zovdeneg.app.ui.screens.DepositScreen
import com.zovdeneg.app.ui.screens.SecurityDetailScreen
import com.zovdeneg.app.ui.screens.SellScreen
import com.zovdeneg.app.ui.trade.BuyViewModel
import com.zovdeneg.app.ui.trade.SellViewModel
import com.zovdeneg.app.ui.trade.SecurityDetailViewModel

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

internal fun NavGraphBuilder.zovTradeDeposit(navController: NavHostController) {
    composable(ZovRoutes.DEPOSIT) {
        val depositVm: DepositViewModel = hiltViewModel()
        DepositScreen(
            viewModel = depositVm,
            onBack = { navController.popBackStack() },
            onAfterBalanceChanged = {
                navController.notifyPortfolioOrBalanceChangedExternally()
                navController.popBackStack()
            },
        )
    }
}

internal fun NavGraphBuilder.zovSecurityDetail(navController: NavHostController) {
    composable(
        ZovRoutes.DETAIL,
        arguments = listOf(
            navArgument("securityId") { type = NavType.StringType },
            navArgument("displayTicker") { type = NavType.StringType },
        ),
    ) { _ ->
        val detailVm: SecurityDetailViewModel = hiltViewModel()
        SecurityDetailScreen(
            viewModel = detailVm,
            onBuy = {
                val d = detailVm.uiState.value.detail
                if (d != null) {
                    navController.navigate(ZovRoutes.buy(d.securityId, d.ticker))
                }
            },
            onSell = {
                val d = detailVm.uiState.value.detail
                if (d != null && d.canSellAtLeastOneLot) {
                    navController.navigate(ZovRoutes.sell(d.securityId, d.ticker))
                }
            },
        )
    }
}

internal fun NavGraphBuilder.zovSecurityBuy(navController: NavHostController) {
    composable(
        ZovRoutes.BUY,
        arguments = listOf(
            navArgument("securityId") { type = NavType.StringType },
            navArgument("displayTicker") { type = NavType.StringType },
        ),
    ) { _ ->
        val buyVm: BuyViewModel = hiltViewModel()
        BuyScreen(
            viewModel = buyVm,
            onBack = { navController.popBackStack() },
            onPortfolioChanged = { navController.notifyPortfolioOrBalanceChangedExternally() },
        )
    }
}

internal fun NavGraphBuilder.zovSecuritySell(navController: NavHostController) {
    composable(
        ZovRoutes.SELL,
        arguments = listOf(
            navArgument("securityId") { type = NavType.StringType },
            navArgument("displayTicker") { type = NavType.StringType },
        ),
    ) { _ ->
        val sellVm: SellViewModel = hiltViewModel()
        SellScreen(
            viewModel = sellVm,
            onBack = { navController.popBackStack() },
            onPortfolioChanged = { navController.notifyPortfolioOrBalanceChangedExternally() },
        )
    }
}
