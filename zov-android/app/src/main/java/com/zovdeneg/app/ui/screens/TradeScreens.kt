package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.market.SecurityOrderBook
import com.zovdeneg.app.domain.market.SecurityOrderBookRow
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.components.LocalZovSnackbarHostState
import com.zovdeneg.app.ui.components.LocalZovSnackbarScope
import com.zovdeneg.app.ui.deposit.DepositScreenLoadedContent
import com.zovdeneg.app.ui.deposit.DepositSuccessSideEffect
import com.zovdeneg.app.ui.components.ZovCenteredCircularProgress
import com.zovdeneg.app.ui.components.ZovPullToRefreshScrollScreen
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.deposit.DepositUiState
import com.zovdeneg.app.ui.deposit.DepositViewModel
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme
import com.zovdeneg.app.ui.trade.BuySubmitHint
import com.zovdeneg.app.ui.trade.BuyUiState
import com.zovdeneg.app.ui.trade.BuyViewModel
import com.zovdeneg.app.ui.trade.SecurityChartRange
import com.zovdeneg.app.ui.trade.SecurityDetailAboutCompanyCard
import com.zovdeneg.app.ui.trade.SecurityDetailOrderBookTab
import com.zovdeneg.app.ui.trade.SecurityDetailPortfolioBanner
import com.zovdeneg.app.ui.trade.SecurityDetailPriceChartBlock
import com.zovdeneg.app.ui.trade.MarketOrderLotsRow
import com.zovdeneg.app.ui.trade.SecurityDetailUiState
import com.zovdeneg.app.ui.trade.SecurityDetailViewModel

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import kotlinx.coroutines.launch

private val previewSecurityOrderBook =
    SecurityOrderBook(
        askLevels = listOf(
            SecurityOrderBookRow(1200, "305.00", 1200),
            SecurityOrderBookRow(900, "303.50", 900),
            SecurityOrderBookRow(1500, "302.10", 1500),
            SecurityOrderBookRow(600, "301.00", 600),
            SecurityOrderBookRow(2100, "300.40", 2100),
        ),
        bestAskPriceDecimal = "299.20",
        bestBidPriceDecimal = "298.00",
        bidLevels = listOf(
            SecurityOrderBookRow(4200, "298.00", null),
            SecurityOrderBookRow(1800, "297.50", null),
            SecurityOrderBookRow(950, "297.00", null),
            SecurityOrderBookRow(3200, "296.40", null),
            SecurityOrderBookRow(700, "295.80", null),
        ),
    )

private val previewSecurityDetail =
    SecurityDetail(
        ticker = "SBER",
        subtitle = "Сбербанк · финансы",
        priceLine = "298,12 ₽",
        changeLine = "+1,2%",
        changePositive = true,
        securityId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
        lotSize = 10,
        orderBookText = null,
        sectorName = "Финансы",
        exchangeCode = "MOEX",
        companyDescription = "Сбербанк — крупнейший банк России и Восточной Европы. " +
            "Предоставляет полный спектр банковских услуг физическим и юридическим лицам.",
        portfolioQuantity = 10,
        portfolioAvgPriceLine = "285 ₽",
        portfolioCurrentValueLine = "2 981,2 ₽",
        portfolioPositionDeltaLine = "+132,5 ₽ (+4,65%)",
        portfolioPositionDeltaPositive = true,
        portfolioUnitPriceLine = "298,12 ₽",
        orderBook = previewSecurityOrderBook,
    )

private data class SecurityDetailScreenCallbacks(
    val onRetry: () -> Unit,
    val onBuy: () -> Unit,
    val onSell: () -> Unit,
    val onSelectChartRange: (SecurityChartRange) -> Unit,
    val onOrderBookTabSelected: () -> Unit,
)

@Composable
fun SecurityDetailScreen(
    viewModel: SecurityDetailViewModel,
    onBuy: () -> Unit,
    onSell: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SecurityDetailScreenContent(
        uiState = state,
        isPullRefreshing = state.isPullRefreshing,
        onPullRefresh = viewModel::pullToRefresh,
        callbacks = SecurityDetailScreenCallbacks(
            onRetry = viewModel::retry,
            onBuy = onBuy,
            onSell = onSell,
            onSelectChartRange = viewModel::selectChartRange,
            onOrderBookTabSelected = viewModel::loadOrderBookIfNeeded,
        ),
    )
}

@Composable
private fun ZovScrollBackgroundPrimaryTabRow(
    selectedTabIndex: Int,
    tabContents: @Composable () -> Unit,
) {
    val c = ZovTheme.colors
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = c.background,
        contentColor = c.primary,
        divider = { HorizontalDivider(color = c.outline) },
        tabs = tabContents,
    )
}

@Composable
private fun SecurityDetailTabs(selectedTab: Int, onSelectTab: (Int) -> Unit) {
    val t = ZovTheme.text
    ZovScrollBackgroundPrimaryTabRow(selectedTabIndex = selectedTab) {
        Tab(
            selected = selectedTab == 0,
            onClick = { onSelectTab(0) },
            text = { Text(stringResource(R.string.security_tab_overview), style = t.bodyReg14) },
        )
        Tab(
            selected = selectedTab == 1,
            onClick = { onSelectTab(1) },
            text = { Text(stringResource(R.string.security_tab_order_book), style = t.bodyReg14) },
        )
    }
}

@Composable
private fun SecurityDetailBuySellActionsRow(onBuy: () -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Button(
        onClick = onBuy,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
    ) {
        Text(stringResource(R.string.action_buy), style = t.bodyMed14)
    }
}

@Composable
private fun SecurityDetailLoadedSection(
    tab: Int,
    uiState: SecurityDetailUiState,
    callbacks: SecurityDetailScreenCallbacks,
) {
    val detail = requireNotNull(uiState.detail)
    val c = ZovTheme.colors
    val t = ZovTheme.text
    if (tab == 0) {
        Text(detail.subtitle, style = t.labelReg12, color = c.onSurfaceVariant)
        Text(detail.priceLine, style = t.titleSemi22, color = c.onSurface)
        Text(
            detail.changeLine,
            style = t.bodyMed14,
            color = if (detail.changePositive) c.positive else c.negative,
        )
        SecurityDetailPriceChartBlock(
            chartLoading = uiState.chartLoading,
            chartFailed = uiState.chartFailed,
            priceHistory = uiState.priceHistory,
            chartRange = uiState.chartRange,
            onSelectChartRange = callbacks.onSelectChartRange,
        )
        SecurityDetailPortfolioBanner(detail = detail, onSell = callbacks.onSell)
        SecurityDetailAboutCompanyCard(detail)
    } else {
        when {
            uiState.orderBookLoading -> ZovCenteredCircularProgress()
            uiState.orderBookLoadFailed ->
                Text(
                    stringResource(R.string.security_order_book_load_failed),
                    style = t.bodyReg14,
                    color = c.negative,
                )
            else -> SecurityDetailOrderBookTab(detail = detail)
        }
    }
    SecurityDetailBuySellActionsRow(onBuy = callbacks.onBuy)
}

@Composable
private fun SecurityDetailScreenContent(
    uiState: SecurityDetailUiState,
    isPullRefreshing: Boolean,
    onPullRefresh: () -> Unit,
    callbacks: SecurityDetailScreenCallbacks,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var tab by remember { mutableIntStateOf(0) }
    LaunchedEffect(tab) {
        if (tab == 1) {
            callbacks.onOrderBookTabSelected()
        }
    }
    ZovPullToRefreshScrollScreen(
        isRefreshing = isPullRefreshing,
        onRefresh = onPullRefresh,
    ) {
        when {
            uiState.isLoading -> {
                Spacer(Modifier.height(ZovItemSpacing))
                ZovCenteredCircularProgress()
            }
            uiState.detail != null -> {
                SecurityDetailTabs(selectedTab = tab, onSelectTab = { tab = it })
                SecurityDetailLoadedSection(
                    tab = tab,
                    uiState = uiState,
                    callbacks = callbacks,
                )
            }
            uiState.detailFailed -> {
                Text(
                    stringResource(R.string.security_detail_card_failed),
                    style = t.bodyReg14,
                    color = c.negative,
                )
                SecurityDetailPriceChartBlock(
                    chartLoading = uiState.chartLoading,
                    chartFailed = uiState.chartFailed,
                    priceHistory = uiState.priceHistory,
                    chartRange = uiState.chartRange,
                    onSelectChartRange = callbacks.onSelectChartRange,
                )
                Button(onClick = callbacks.onRetry) {
                    Text(stringResource(R.string.action_retry), style = t.bodyMed14)
                }
            }
        }
    }
}

@Composable
fun BuyScreen(
    viewModel: BuyViewModel,
    onBack: () -> Unit,
    onPortfolioChanged: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalZovSnackbarHostState.current
    val snackbarScope = LocalZovSnackbarScope.current
    val successMsg = stringResource(R.string.order_submitted)
    LaunchedEffect(state.orderJustPlaced) {
        if (!state.orderJustPlaced) return@LaunchedEffect
        snackbarScope.launch { snackbarHostState.showSnackbar(successMsg) }
        onPortfolioChanged()
        viewModel.acknowledgeOrderPlaced()
        onBack()
    }
    val label =
        state.displayTicker.ifBlank { state.detail?.ticker.orEmpty() }.replace('_', '/')
    ZovScrollScreen {
        BuyScreenOrderContent(
            state = state,
            label = label,
            viewModel = viewModel,
            onBack = onBack,
        )
    }
}

@Composable
private fun BuySubmitHintBanner(hint: BuySubmitHint) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    when (val h = hint) {
        BuySubmitHint.None -> {}
        BuySubmitHint.EstimatedInsufficientBalance ->
            Text(
                stringResource(R.string.buy_precheck_insufficient_balance),
                style = t.bodyReg14,
                color = c.negative,
            )

        BuySubmitHint.NoAvailableFunds ->
            Text(
                stringResource(R.string.buy_error_no_available_funds),
                style = t.bodyReg14,
                color = c.negative,
            )

        BuySubmitHint.BalanceUnavailable ->
            Text(
                stringResource(R.string.buy_error_balance_unavailable),
                style = t.bodyReg14,
                color = c.negative,
            )

        is BuySubmitHint.InsufficientFundsFromApi -> {
            val msg = h.serverMessage?.trim().orEmpty()
            Text(
                if (msg.isNotEmpty()) {
                    stringResource(R.string.buy_error_insufficient_funds_reason, msg)
                } else {
                    stringResource(R.string.buy_error_insufficient_funds)
                },
                style = t.bodyReg14,
                color = c.negative,
            )
        }

        BuySubmitHint.GenericFailure ->
            Text(stringResource(R.string.error_submit_order), style = t.bodyReg14, color = c.negative)
    }
}

@Composable
private fun BuyScreenOrderContent(
    state: BuyUiState,
    label: String,
    viewModel: BuyViewModel,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    when {
        state.isLoading -> ZovCenteredCircularProgress()
        state.loadFailed -> {
            Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
            Button(onClick = onBack) { Text(stringResource(R.string.action_back)) }
        }
        state.detail != null -> {
            val detail = requireNotNull(state.detail)
            Text(
                stringResource(R.string.buy_title_format, label),
                style = t.sectionSemi16,
                color = c.onSurface,
            )
            Text(stringResource(R.string.buy_lots_label), style = t.labelMed12, color = c.onSurfaceVariant)
            MarketOrderLotsRow(
                lots = state.lots,
                isSubmitting = state.isSubmitting,
                onBump = viewModel::bumpLots,
                onSetLots = viewModel::setLots,
            )
            Text(
                stringResource(
                    R.string.buy_order_hint,
                    state.lots * detail.lotSize,
                    detail.priceLine,
                ),
                style = t.subtitleReg14,
                color = c.onSurfaceVariant,
            )
            BuySubmitHintBanner(state.submitHint)
            Button(
                onClick = { viewModel.submitOrder() },
                enabled = state.canSubmitMarketBuy,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primary,
                    contentColor = c.onPrimary,
                ),
            ) { Text(stringResource(R.string.action_place_order), style = t.bodyMed14) }
        }
    }
}

@Composable
fun DepositScreen(
    viewModel: DepositViewModel,
    onBack: () -> Unit,
    onAfterBalanceChanged: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalZovSnackbarHostState.current
    val snackbarScope = LocalZovSnackbarScope.current
    var tab by remember { mutableIntStateOf(0) }
    DepositSuccessSideEffect(
        pendingSuccess = state.pendingSuccess,
        snackbarHostState = snackbarHostState,
        snackbarScope = snackbarScope,
        onAcknowledge = viewModel::acknowledgePendingSuccess,
        onAfterBalanceChanged = onAfterBalanceChanged,
    )
    ZovScrollScreen {
        DepositScreenBody(
            state = state,
            tab = tab,
            onTabChange = { tab = it },
            viewModel = viewModel,
            onBack = onBack,
        )
    }
}

@Composable
private fun DepositScreenBody(
    state: DepositUiState,
    tab: Int,
    onTabChange: (Int) -> Unit,
    viewModel: DepositViewModel,
    onBack: () -> Unit,
) {
    val t = ZovTheme.text
    val chipResIds =
        listOf(
            R.string.deposit_chip_1000,
            R.string.deposit_chip_5000,
            R.string.deposit_chip_10000,
            R.string.deposit_chip_50000,
        )
    ZovScrollBackgroundPrimaryTabRow(selectedTabIndex = tab) {
        Tab(
            selected = tab == 0,
            onClick = { onTabChange(0) },
            text = { Text(stringResource(R.string.deposit_tab_deposit), style = t.bodyReg14) },
        )
        Tab(
            selected = tab == 1,
            onClick = { onTabChange(1) },
            text = { Text(stringResource(R.string.deposit_tab_withdraw), style = t.bodyReg14) },
        )
    }
    when {
        state.isLoading -> ZovCenteredCircularProgress()
        state.loadFailed -> {
            Text(stringResource(R.string.error_load))
            Button(onClick = { viewModel.refresh() }) { Text(stringResource(R.string.action_retry)) }
        }
        else -> {
            DepositScreenLoadedContent(
                state = state,
                tab = tab,
                chipResIds = chipResIds,
                viewModel = viewModel,
                onBack = onBack,
            )
        }
    }
}

@Preview(name = "Светлая", showBackground = true, locale = "ru")
@Composable
private fun DetailPreviewLight() {
    ZovAppTheme(darkTheme = false) {
        SecurityDetailScreenContent(
            uiState = SecurityDetailUiState(
                isLoading = false,
                detail = previewSecurityDetail,
                detailFailed = false,
            ),
            isPullRefreshing = false,
            onPullRefresh = {},
            callbacks = SecurityDetailScreenCallbacks(
                onRetry = {},
                onBuy = {},
                onSell = {},
                onSelectChartRange = {},
                onOrderBookTabSelected = {},
            ),
        )
    }
}

@Preview(name = "Тёмная", showBackground = true, locale = "ru")
@Composable
private fun DetailPreviewDark() {
    ZovAppTheme(darkTheme = true) {
        SecurityDetailScreenContent(
            uiState = SecurityDetailUiState(
                isLoading = false,
                detail = previewSecurityDetail,
                detailFailed = false,
            ),
            isPullRefreshing = false,
            onPullRefresh = {},
            callbacks = SecurityDetailScreenCallbacks(
                onRetry = {},
                onBuy = {},
                onSell = {},
                onSelectChartRange = {},
                onOrderBookTabSelected = {},
            ),
        )
    }
}
