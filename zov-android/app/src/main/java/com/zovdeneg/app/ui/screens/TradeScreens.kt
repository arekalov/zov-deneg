package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.balance.BrokerageBalance
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.market.SecurityOrderBook
import com.zovdeneg.app.domain.market.SecurityOrderBookRow
import com.zovdeneg.app.ui.common.ZovFieldInnerPadding
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.components.LocalZovSnackbarHostState
import com.zovdeneg.app.ui.components.LocalZovSnackbarScope
import com.zovdeneg.app.ui.components.ZovFilterChip
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.deposit.DepositViewModel
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme
import com.zovdeneg.app.ui.trade.BuyUiState
import com.zovdeneg.app.ui.trade.BuyViewModel
import com.zovdeneg.app.ui.trade.SecurityChartRange
import com.zovdeneg.app.ui.trade.SecurityDetailAboutCompanyCard
import com.zovdeneg.app.ui.trade.SecurityDetailOrderBookTab
import com.zovdeneg.app.ui.trade.SecurityDetailPortfolioBanner
import com.zovdeneg.app.ui.trade.SecurityDetailPriceChartBlock
import com.zovdeneg.app.ui.trade.SecurityDetailUiState
import com.zovdeneg.app.ui.trade.SecurityDetailViewModel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
        orderBook = previewSecurityOrderBook,
    )

@Composable
fun SecurityDetailScreen(
    viewModel: SecurityDetailViewModel,
    onBuy: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SecurityDetailScreenContent(
        uiState = state,
        onRetry = viewModel::retry,
        onBuy = onBuy,
        onSelectChartRange = viewModel::selectChartRange,
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
private fun SecurityDetailLoadedSection(
    tab: Int,
    uiState: SecurityDetailUiState,
    onBuy: () -> Unit,
    onSelectChartRange: (SecurityChartRange) -> Unit,
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
            onSelectChartRange = onSelectChartRange,
        )
        SecurityDetailPortfolioBanner(detail)
        SecurityDetailAboutCompanyCard(detail)
    } else {
        SecurityDetailOrderBookTab(detail = detail)
    }
    Button(
        onClick = onBuy,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = c.primary,
            contentColor = c.onPrimary,
        ),
    ) { Text(stringResource(R.string.action_buy), style = t.bodyMed14) }
}

@Composable
private fun SecurityDetailScreenContent(
    uiState: SecurityDetailUiState,
    onRetry: () -> Unit,
    onBuy: () -> Unit,
    onSelectChartRange: (SecurityChartRange) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var tab by remember { mutableIntStateOf(0) }
    ZovScrollScreen {
        SecurityDetailTabs(selectedTab = tab, onSelectTab = { tab = it })
        when {
            uiState.isLoading -> {
                Spacer(Modifier.height(ZovItemSpacing))
                CircularProgressIndicator(color = c.primary)
            }
            uiState.loadFailed -> {
                Spacer(Modifier.height(ZovItemSpacing))
                Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
                Button(onClick = onRetry) {
                    Text(stringResource(R.string.action_retry), style = t.bodyMed14)
                }
            }
            uiState.detail != null -> {
                SecurityDetailLoadedSection(
                    tab = tab,
                    uiState = uiState,
                    onBuy = onBuy,
                    onSelectChartRange = onSelectChartRange,
                )
            }
        }
    }
}

@Composable
private fun BuyLotsRow(
    lots: Int,
    isSubmitting: Boolean,
    onBump: (Int) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
    ) {
        OutlinedButton(
            onClick = { onBump(-1) },
            enabled = !isSubmitting && lots > 1,
        ) {
            Text("−", style = t.titleSemi20)
        }
        Text(
            lots.toString(),
            style = t.pinAmount20,
            modifier = Modifier
                .weight(1f)
                .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
                .padding(ZovFieldInnerPadding),
        )
        OutlinedButton(
            onClick = { onBump(1) },
            enabled = !isSubmitting,
        ) {
            Text("+", style = t.titleSemi20)
        }
    }
}

@Composable
fun BuyScreen(
    viewModel: BuyViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalZovSnackbarHostState.current
    val snackbarScope = LocalZovSnackbarScope.current
    val successMsg = stringResource(R.string.order_submitted)
    LaunchedEffect(state.orderJustPlaced) {
        if (!state.orderJustPlaced) return@LaunchedEffect
        snackbarScope.launch {
            snackbarHostState.showSnackbar(successMsg)
        }
        viewModel.acknowledgeOrderPlaced()
        onBack()
    }
    val label = state.detail?.ticker?.replace('_', '/') ?: ""
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
private fun BuyScreenOrderContent(
    state: BuyUiState,
    label: String,
    viewModel: BuyViewModel,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    when {
        state.isLoading -> CircularProgressIndicator(color = c.primary)
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
            BuyLotsRow(
                lots = state.lots,
                isSubmitting = state.isSubmitting,
                onBump = viewModel::bumpLots,
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
            if (state.submitFailed) {
                Text(stringResource(R.string.error_submit_order), style = t.bodyReg14, color = c.negative)
            }
            Button(
                onClick = { viewModel.submitOrder() },
                enabled = !state.isSubmitting,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = c.primary,
                    contentColor = c.onPrimary,
                ),
            ) { Text(stringResource(R.string.action_place_order), style = t.bodyMed14) }
        }
    }
}

private val depositAmounts = listOf("1000.00", "5000.00", "10000.00", "50000.00")

private data class DepositTabModel(
    val chipResIds: List<Int>,
    val amountChip: Int,
    val balance: BrokerageBalance?,
    val isWorking: Boolean,
    val actionFailed: Boolean,
)

@Composable
private fun DepositTabContent(
    model: DepositTabModel,
    onAmountChip: (Int) -> Unit,
    onDeposit: (String) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val balance = model.balance
    if (balance != null) {
        Text(
            stringResource(R.string.balance_available, balance.availableText),
            style = t.bodyReg14,
            color = c.onSurface,
        )
        Text(
            stringResource(R.string.balance_blocked, balance.blockedText),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.balance_total, balance.totalText),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
        )
    }
    Text(stringResource(R.string.deposit_amount_label), style = t.bodyMed14, color = c.onSurface)
    Text(
        stringResource(R.string.deposit_amount_mock),
        style = t.pinAmount20,
        color = c.onSurface,
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
            .padding(ZovHorizontalPadding),
    )
    Row(horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
        model.chipResIds.forEachIndexed { i, resId ->
            ZovFilterChip(
                label = stringResource(resId),
                selected = model.amountChip == i,
                onClick = { onAmountChip(i) },
            )
        }
    }
    if (model.actionFailed) {
        Text(stringResource(R.string.error_submit_deposit), style = t.bodyReg14, color = c.negative)
    }
    Button(
        onClick = {
            val idx = model.amountChip.coerceIn(0, depositAmounts.lastIndex)
            onDeposit(depositAmounts[idx])
        },
        enabled = !model.isWorking,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.deposit_action))
    }
}

@Composable
private fun WithdrawTabContent(
    balance: BrokerageBalance?,
    isWorking: Boolean,
    actionFailed: Boolean,
    onWithdraw: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    if (balance != null) {
        Text(
            stringResource(R.string.balance_available, balance.availableText),
            style = t.bodyReg14,
            color = c.onSurface,
        )
    }
    Text(stringResource(R.string.withdraw_amount_label), style = t.bodyMed14, color = c.onSurface)
    Text(
        stringResource(R.string.withdraw_amount_mock),
        style = t.pinAmount20,
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
            .padding(ZovHorizontalPadding),
    )
    if (actionFailed) {
        Text(
            stringResource(R.string.error_submit_withdraw),
            style = t.bodyReg14,
            color = c.negative,
        )
    }
    Button(onClick = onWithdraw, enabled = !isWorking, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.withdraw_action))
    }
}

@Composable
fun DepositScreen(
    viewModel: DepositViewModel,
    onBack: () -> Unit,
) {
    val t = ZovTheme.text
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    val chipResIds =
        listOf(
            R.string.deposit_chip_1000,
            R.string.deposit_chip_5000,
            R.string.deposit_chip_10000,
            R.string.deposit_chip_50000,
        )
    ZovScrollScreen {
        ZovScrollBackgroundPrimaryTabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text(stringResource(R.string.deposit_tab_deposit), style = t.bodyReg14) },
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text(stringResource(R.string.deposit_tab_withdraw), style = t.bodyReg14) },
            )
        }
        when {
            state.isLoading -> CircularProgressIndicator()
            state.loadFailed -> {
                Text(stringResource(R.string.error_load))
                Button(onClick = { viewModel.refresh() }) { Text(stringResource(R.string.action_retry)) }
            }
            else -> {
                if (tab == 0) {
                    DepositTabContent(
                        model = DepositTabModel(
                            chipResIds = chipResIds,
                            amountChip = state.selectedDepositChipIndex,
                            balance = state.balance,
                            isWorking = state.isWorking,
                            actionFailed = state.actionFailed,
                        ),
                        onAmountChip = viewModel::selectDepositChip,
                        onDeposit = viewModel::depositSelectedAmount,
                    )
                } else {
                    WithdrawTabContent(
                        balance = state.balance,
                        isWorking = state.isWorking,
                        actionFailed = state.actionFailed,
                        onWithdraw = viewModel::withdrawDemoAmount,
                    )
                }
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_back))
                }
            }
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
                loadFailed = false,
            ),
            onRetry = {},
            onBuy = {},
            onSelectChartRange = {},
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
                loadFailed = false,
            ),
            onRetry = {},
            onBuy = {},
            onSelectChartRange = {},
        )
    }
}
