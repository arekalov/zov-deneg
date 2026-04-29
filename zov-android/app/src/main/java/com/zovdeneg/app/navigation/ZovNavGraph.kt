package com.zovdeneg.app.navigation

import android.annotation.SuppressLint
import com.zovdeneg.app.di.ZovLocalAuthEntryPoint
import com.zovdeneg.app.ui.auth.ZovLoginViewModel
import com.zovdeneg.app.ui.auth.ZovRegisterFlowViewModel
import com.zovdeneg.app.ui.auth.ZovRegisterStep1ViewModel
import com.zovdeneg.app.ui.deposit.DepositViewModel
import com.zovdeneg.app.ui.home.MainHomeViewModel
import com.zovdeneg.app.ui.profile.ChangePinViewModel
import com.zovdeneg.app.ui.profile.EditProfileViewModel
import com.zovdeneg.app.ui.profile.ProfileViewModel
import com.zovdeneg.app.ui.screens.BuyScreen
import com.zovdeneg.app.ui.screens.ChangePinScreen
import com.zovdeneg.app.ui.screens.DepositScreen
import com.zovdeneg.app.ui.screens.EditProfileScreen
import com.zovdeneg.app.ui.screens.HistoryTabScreen
import com.zovdeneg.app.ui.screens.LoginScreen
import com.zovdeneg.app.ui.screens.OrderDetailScreen
import com.zovdeneg.app.ui.screens.OrdersListScreen
import com.zovdeneg.app.ui.orders.OrderDetailViewModel
import com.zovdeneg.app.ui.orders.OrdersListViewModel
import com.zovdeneg.app.ui.screens.MainHomeScreen
import com.zovdeneg.app.ui.screens.ProfileScreen
import com.zovdeneg.app.ui.screens.RegisterBiometricScreen
import com.zovdeneg.app.ui.screens.RegisterDataScreen
import com.zovdeneg.app.ui.screens.RegisterPinConfirmScreen
import com.zovdeneg.app.ui.screens.RegisterPinScreen
import com.zovdeneg.app.ui.screens.SearchTabScreen
import com.zovdeneg.app.ui.screens.SecurityDetailScreen
import com.zovdeneg.app.ui.tabs.ZovHistoryTabViewModel
import com.zovdeneg.app.ui.tabs.ZovSearchTabViewModel
import com.zovdeneg.app.ui.trade.BuyViewModel
import com.zovdeneg.app.ui.trade.SecurityDetailViewModel
import dagger.hilt.android.EntryPointAccessors

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navigation
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.zovdeneg.app.BuildConfig
import kotlinx.coroutines.delay

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
        val loginVm: ZovLoginViewModel = hiltViewModel()
        LoginScreen(
            viewModel = loginVm,
            onLoggedIn = {
                navController.navigate(ZovRoutes.MAIN_HOME) {
                    popUpTo(ZovRoutes.LOGIN) { inclusive = true }
                }
            },
            onRegister = { navController.navigate(ZovRoutes.REGISTER_FLOW) },
            onNeedPinSetupAfterLogin = {
                navController.navigate(ZovRoutes.REGISTER_FLOW) {
                    launchSingleTop = true
                }
                navController.navigate(ZovRoutes.registerStep2WithFreshPin(true)) {
                    launchSingleTop = true
                    popUpTo(ZovRoutes.REGISTER_STEP1) { inclusive = true }
                }
            },
        )
    }
    zovRegisterFlow(navController)
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
private fun registerFlowBackStackEntry(navController: NavHostController): NavBackStackEntry =
    remember(navController) {
        navController.getBackStackEntry(ZovRoutes.REGISTER_FLOW)
    }

private fun NavGraphBuilder.zovRegisterFlow(navController: NavHostController) {
    navigation(
        route = ZovRoutes.REGISTER_FLOW,
        startDestination = ZovRoutes.REGISTER_STEP1,
    ) {
        registerFlowStep1(navController)
        registerFlowStep2(navController)
        registerFlowStep3(navController)
        registerFlowStep4(navController)
    }
}

private fun NavGraphBuilder.registerFlowStep1(navController: NavHostController) {
    composable(ZovRoutes.REGISTER_STEP1) {
        val registerVm: ZovRegisterFlowViewModel =
            hiltViewModel(registerFlowBackStackEntry(navController))
        val step1Vm: ZovRegisterStep1ViewModel = hiltViewModel()
        LaunchedEffect(Unit) { registerVm.resetFlow() }
        RegisterDataScreen(
            viewModel = step1Vm,
            onNext = { navController.navigate(ZovRoutes.REGISTER_STEP2) },
            onLogin = { navController.popBackStack(ZovRoutes.LOGIN, false) },
        )
    }
}

private fun NavGraphBuilder.registerFlowStep2(navController: NavHostController) {
    composable(
        route = ZovRoutes.REGISTER_STEP2_ROUTE,
        arguments = listOf(
            navArgument("freshPin") {
                type = NavType.BoolType
                defaultValue = false
            },
        ),
    ) { entry ->
        val registerVm: ZovRegisterFlowViewModel =
            hiltViewModel(registerFlowBackStackEntry(navController))
        val freshPin = entry.arguments?.getBoolean("freshPin") ?: false
        LaunchedEffect(freshPin) {
            if (freshPin) {
                registerVm.resetFlow()
            } else {
                registerVm.clearDraft()
            }
        }
        RegisterPinScreen(
            viewModel = registerVm,
            onContinue = {
                if (registerVm.commitFirstPin()) {
                    navController.navigate(ZovRoutes.REGISTER_STEP3)
                }
            },
        )
    }
}

private fun NavGraphBuilder.registerFlowStep3(navController: NavHostController) {
    composable(ZovRoutes.REGISTER_STEP3) {
        val context = LocalContext.current
        val registerVm: ZovRegisterFlowViewModel =
            hiltViewModel(registerFlowBackStackEntry(navController))
        LaunchedEffect(Unit) { registerVm.clearDraft() }
        RegisterPinConfirmScreen(
            viewModel = registerVm,
            onContinue = {
                if (registerVm.tryFinishSecondPinStep()) {
                    if (BuildConfig.IS_BIOMETRY_AVAILABLE) {
                        EntryPointAccessors.fromApplication(
                            context.applicationContext,
                            ZovLocalAuthEntryPoint::class.java,
                        ).localAuthStorage().setBiometricUnlockEnabled(false)

                        navController.navigate(ZovRoutes.MAIN_HOME) {
                            popUpTo(ZovRoutes.LOGIN) { inclusive = true }
                        }
                    } else {
                        navController.navigate(ZovRoutes.REGISTER_STEP4)
                    }
                }
            },
        )
    }
}

private fun NavGraphBuilder.registerFlowStep4(navController: NavHostController) {
    composable(ZovRoutes.REGISTER_STEP4) {
        val context = LocalContext.current
        val navigateHome = {
            navController.navigate(ZovRoutes.MAIN_HOME) {
                popUpTo(ZovRoutes.LOGIN) { inclusive = true }
            }
        }
        RegisterBiometricScreen(
            onAllow = {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ZovLocalAuthEntryPoint::class.java,
                ).localAuthStorage().setBiometricUnlockEnabled(true)
                navigateHome()
            },
            onSkip = {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    ZovLocalAuthEntryPoint::class.java,
                ).localAuthStorage().setBiometricUnlockEnabled(false)
                navigateHome()
            },
        )
    }
}

private fun NavGraphBuilder.zovTabDestinations(navController: NavHostController) {
    composable(ZovRoutes.MAIN_HOME) { homeEntry ->
        val homeVm: MainHomeViewModel = hiltViewModel()
        val homeLifecycle = LocalLifecycleOwner.current.lifecycle
        val homeRefreshTick by homeEntry.savedStateHandle
            .getStateFlow(MAIN_HOME_DATA_REFRESH_TICK_KEY, 0L)
            .collectAsStateWithLifecycle()
        LaunchedEffect(homeRefreshTick) {
            if (homeRefreshTick != 0L) {
                homeVm.refresh()
                homeEntry.savedStateHandle[MAIN_HOME_DATA_REFRESH_TICK_KEY] = 0L
            }
        }
        LaunchedEffect(homeVm, homeLifecycle) {
            homeLifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    delay(MainHomeViewModel.QUIET_REFRESH_INTERVAL_MS)
                    homeVm.refreshQuietly()
                }
            }
        }
        MainHomeScreen(
            viewModel = homeVm,
            onOpenBrokerAccount = { navController.navigate(ZovRoutes.DEPOSIT) },
            onOpenOrders = { navController.navigate(ZovRoutes.ORDERS_LIST) },
            onOpenSecurity = { securityId, displayTicker ->
                navController.navigate(ZovRoutes.detail(securityId, displayTicker))
            },
        )
    }
    composable(ZovRoutes.MAIN_SEARCH) {
        val searchVm: ZovSearchTabViewModel = hiltViewModel()
        SearchTabScreen(
            viewModel = searchVm,
            onOpenSecurity = { securityId, displayTicker ->
                navController.navigate(ZovRoutes.detail(securityId, displayTicker))
            },
        )
    }
    composable(ZovRoutes.MAIN_HISTORY) { historyEntry ->
        val historyVm: ZovHistoryTabViewModel = hiltViewModel()
        val historyRefreshTick by historyEntry.savedStateHandle
            .getStateFlow(MAIN_HISTORY_DATA_REFRESH_TICK_KEY, 0L)
            .collectAsStateWithLifecycle()
        LaunchedEffect(historyRefreshTick) {
            if (historyRefreshTick != 0L) {
                historyVm.refresh()
                historyEntry.savedStateHandle[MAIN_HISTORY_DATA_REFRESH_TICK_KEY] = 0L
            }
        }
        HistoryTabScreen(viewModel = historyVm)
    }
}

private fun NavGraphBuilder.zovProfileDestinations(navController: NavHostController) {
    composable(ZovRoutes.PROFILE) { profileEntry ->
        val profileVm: ProfileViewModel = hiltViewModel()
        val refreshTick by profileEntry.savedStateHandle
            .getStateFlow(PROFILE_REFRESH_TICK_KEY, 0L)
            .collectAsStateWithLifecycle()
        LaunchedEffect(refreshTick) {
            if (refreshTick != 0L) {
                profileVm.refresh()
                profileEntry.savedStateHandle[PROFILE_REFRESH_TICK_KEY] = 0L
            }
        }
        ProfileScreen(
            viewModel = profileVm,
            onEditProfile = { navController.navigate(ZovRoutes.EDIT_PROFILE) },
            onChangePin = { navController.navigate(ZovRoutes.CHANGE_PIN) },
            onLogout = {
                profileVm.logout {
                    navController.navigate(ZovRoutes.LOGIN) {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                }
            },
        )
    }
    composable(ZovRoutes.EDIT_PROFILE) {
        val editVm: EditProfileViewModel = hiltViewModel()
        EditProfileScreen(
            viewModel = editVm,
            onBack = { navController.popBackStack() },
            onAfterSave = {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.set(PROFILE_REFRESH_TICK_KEY, System.nanoTime())
                navController.popBackStack()
            },
        )
    }
    composable(ZovRoutes.CHANGE_PIN) {
        val pinVm: ChangePinViewModel = hiltViewModel()
        ChangePinScreen(
            viewModel = pinVm,
            onBack = { navController.popBackStack() },
        )
    }
}

private fun NavGraphBuilder.zovOrdersDestinations(navController: NavHostController) {
    composable(
        ZovRoutes.ORDER_DETAIL,
        arguments = listOf(navArgument("orderId") { type = NavType.StringType }),
    ) { _ ->
        val detailVm: OrderDetailViewModel = hiltViewModel()
        OrderDetailScreen(
            viewModel = detailVm,
            onBack = { navController.popBackStack() },
            onCancelSuccess = {
                runCatching {
                    val entry = navController.getBackStackEntry(ZovRoutes.ORDERS_LIST)
                    entry.savedStateHandle[ORDERS_LIST_REFRESH_TICK_KEY] = System.nanoTime()
                }
                navController.popBackStack()
            },
        )
    }
    composable(ZovRoutes.ORDERS_LIST) { ordersEntry ->
        val ordersVm: OrdersListViewModel = hiltViewModel()
        val ordersRefreshTick by ordersEntry.savedStateHandle
            .getStateFlow(ORDERS_LIST_REFRESH_TICK_KEY, 0L)
            .collectAsStateWithLifecycle()
        LaunchedEffect(ordersRefreshTick) {
            if (ordersRefreshTick != 0L) {
                ordersVm.refresh()
                ordersEntry.savedStateHandle[ORDERS_LIST_REFRESH_TICK_KEY] = 0L
            }
        }
        OrdersListScreen(
            viewModel = ordersVm,
            onOpenOrder = { orderId ->
                navController.navigate(ZovRoutes.orderDetail(orderId))
            },
            onPortfolioMayHaveChanged = { navController.notifyPortfolioOrBalanceChangedExternally() },
        )
    }
}

private fun NavGraphBuilder.zovTradeDestinations(navController: NavHostController) {
    zovOrdersDestinations(navController)
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
        )
    }
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

private fun NavGraphBuilder.zovProfileAndTradeDestinations(navController: NavHostController) {
    zovProfileDestinations(navController)
    zovTradeDestinations(navController)
}
