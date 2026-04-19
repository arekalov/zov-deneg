package com.zovdeneg.app.ui.screens

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
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovFieldInnerPadding
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.components.ZovFilterChip
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.domain.balance.BrokerageBalance
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.ui.deposit.DepositViewModel
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme
import com.zovdeneg.app.ui.trade.BuyViewModel
import com.zovdeneg.app.ui.trade.SecurityDetailUiState
import com.zovdeneg.app.ui.trade.SecurityDetailViewModel

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
    )
}

@Composable
private fun SecurityDetailScreenContent(
    uiState: SecurityDetailUiState,
    onRetry: () -> Unit,
    onBuy: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var tab by remember { mutableIntStateOf(0) }
    ZovScrollScreen {
        PrimaryTabRow(selectedTabIndex = tab) {
            Tab(
                selected = tab == 0,
                onClick = { tab = 0 },
                text = { Text(stringResource(R.string.security_tab_overview), style = t.bodyReg14) },
            )
            Tab(
                selected = tab == 1,
                onClick = { tab = 1 },
                text = { Text(stringResource(R.string.security_tab_order_book), style = t.bodyReg14) },
            )
        }
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
                val d = uiState.detail
                if (tab == 0) {
                    Text(
                        d.subtitle,
                        style = t.labelReg12,
                        color = c.onSurfaceVariant,
                    )
                    Text(d.priceLine, style = t.titleSemi22, color = c.onSurface)
                    Text(
                        d.changeLine,
                        style = t.bodyMed14,
                        color = if (d.changePositive) c.positive else c.negative,
                    )
                    Text(
                        stringResource(R.string.security_chart_placeholder),
                        style = t.subtitleReg13,
                        color = c.onSurfaceVariant,
                    )
                } else {
                    Text(
                        d.orderBookText ?: stringResource(R.string.security_order_book_placeholder),
                        style = t.subtitleReg14,
                        color = c.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = onBuy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
                ) { Text(stringResource(R.string.action_buy), style = t.bodyMed14) }
            }
        }
    }
}

@Composable
fun BuyScreen(
    viewModel: BuyViewModel,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMsg = stringResource(R.string.order_submitted)
    LaunchedEffect(state.orderJustPlaced) {
        if (!state.orderJustPlaced) return@LaunchedEffect
        snackbarHostState.showSnackbar(successMsg)
        viewModel.acknowledgeOrderPlaced()
        onBack()
    }
    val label = state.detail?.ticker?.replace('_', '/') ?: ""
    ZovScrollScreen {
        SnackbarHost(hostState = snackbarHostState)
        when {
            state.isLoading -> CircularProgressIndicator(color = c.primary)
            state.loadFailed -> {
                Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
                Button(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            }
            state.detail != null -> {
                val detail = requireNotNull(state.detail)
                Text(stringResource(R.string.buy_title_format, label), style = t.sectionSemi16, color = c.onSurface)
                Text(stringResource(R.string.buy_lots_label), style = t.labelMed12, color = c.onSurfaceVariant)
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
                ) {
                    OutlinedButton(onClick = { viewModel.bumpLots(-1) }, enabled = !state.isSubmitting && state.lots > 1) {
                        Text("−", style = t.titleSemi20)
                    }
                    Text(
                        state.lots.toString(),
                        style = t.pinAmount20,
                        modifier = Modifier
                            .weight(1f)
                            .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
                            .padding(ZovFieldInnerPadding),
                    )
                    OutlinedButton(onClick = { viewModel.bumpLots(1) }, enabled = !state.isSubmitting) {
                        Text("+", style = t.titleSemi20)
                    }
                }
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
                    colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
                ) { Text(stringResource(R.string.action_place_order), style = t.bodyMed14) }
            }
        }
    }
}

private val depositAmounts = listOf("1000.00", "5000.00", "10000.00", "50000.00")

@Composable
private fun DepositTabContent(
    chipResIds: List<Int>,
    amountChip: Int,
    onAmountChip: (Int) -> Unit,
    balance: BrokerageBalance?,
    isWorking: Boolean,
    actionFailed: Boolean,
    onDeposit: (String) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    if (balance != null) {
        Text(stringResource(R.string.balance_available, balance.availableText), style = t.bodyReg14, color = c.onSurface)
        Text(stringResource(R.string.balance_blocked, balance.blockedText), style = t.subtitleReg13, color = c.onSurfaceVariant)
        Text(stringResource(R.string.balance_total, balance.totalText), style = t.subtitleReg13, color = c.onSurfaceVariant)
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
        chipResIds.forEachIndexed { i, resId ->
            ZovFilterChip(
                label = stringResource(resId),
                selected = amountChip == i,
                onClick = { onAmountChip(i) },
            )
        }
    }
    if (actionFailed) {
        Text(stringResource(R.string.error_submit_deposit), style = t.bodyReg14, color = c.negative)
    }
    Button(
        onClick = { onDeposit(depositAmounts[amountChip.coerceIn(0, depositAmounts.lastIndex)]) },
        enabled = !isWorking,
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
        Text(stringResource(R.string.balance_available, balance.availableText), style = t.bodyReg14, color = c.onSurface)
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
        Text(stringResource(R.string.error_submit_withdraw), style = t.bodyReg14, color = c.negative)
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
        PrimaryTabRow(selectedTabIndex = tab) {
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
                        chipResIds = chipResIds,
                        amountChip = state.selectedDepositChipIndex,
                        onAmountChip = viewModel::selectDepositChip,
                        balance = state.balance,
                        isWorking = state.isWorking,
                        actionFailed = state.actionFailed,
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
            uiState =
                SecurityDetailUiState(
                    isLoading = false,
                    detail =
                        SecurityDetail(
                            ticker = "SBER",
                            subtitle = "Сбербанк · финансы",
                            priceLine = "298,12 ₽",
                            changeLine = "+1,2%",
                            changePositive = true,
                            securityId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                            lotSize = 10,
                            orderBookText = null,
                        ),
                    loadFailed = false,
                ),
            onRetry = {},
            onBuy = {},
        )
    }
}

@Preview(name = "Тёмная", showBackground = true, locale = "ru")
@Composable
private fun DetailPreviewDark() {
    ZovAppTheme(darkTheme = true) {
        SecurityDetailScreenContent(
            uiState =
                SecurityDetailUiState(
                    isLoading = false,
                    detail =
                        SecurityDetail(
                            ticker = "SBER",
                            subtitle = "Сбербанк · финансы",
                            priceLine = "298,12 ₽",
                            changeLine = "+1,2%",
                            changePositive = true,
                            securityId = "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
                            lotSize = 10,
                            orderBookText = null,
                        ),
                    loadFailed = false,
                ),
            onRetry = {},
            onBuy = {},
        )
    }
}
