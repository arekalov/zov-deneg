package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.portfolio.Holding
import com.zovdeneg.app.ui.common.ZovHalfUnit
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.components.ZovBalanceStrip
import com.zovdeneg.app.ui.components.ZovFilterChip
import com.zovdeneg.app.ui.components.ZovOutlinedRow
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.components.ZovSummaryCard
import com.zovdeneg.app.ui.home.MainHomeViewModel
import com.zovdeneg.app.ui.tabs.ZovHistoryTabViewModel
import com.zovdeneg.app.ui.tabs.ZovSearchTabViewModel
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private data class MainHomeSummaryBlockTexts(
    val amountText: String,
    val gainText: String,
    val brokerageBalanceLine: String,
)

@Composable
internal fun MainHomeScreen(
    viewModel: MainHomeViewModel,
    onOpenBrokerAccount: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenSecurity: (securityId: String, displayTicker: String) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val placeholder = stringResource(R.string.home_em_dash)
    MainHomeScrollContent(
        holdings = uiState.holdings,
        summaryBlock = MainHomeSummaryBlockTexts(
            amountText = uiState.portfolioAmountRub ?: placeholder,
            gainText = uiState.totalGainText ?: placeholder,
            brokerageBalanceLine = uiState.brokerageTotalRub ?: placeholder,
        ),
        onOpenBrokerAccount = onOpenBrokerAccount,
        onOpenOrders = onOpenOrders,
        onOpenSecurity = onOpenSecurity,
    )
}

@Composable
private fun MainHomeScrollContent(
    holdings: List<Holding>,
    summaryBlock: MainHomeSummaryBlockTexts,
    onOpenBrokerAccount: () -> Unit,
    onOpenOrders: () -> Unit,
    onOpenSecurity: (securityId: String, displayTicker: String) -> Unit,
) {
    ZovScrollScreen {
        MainHomePortfolioSummaryCard(
            amountText = summaryBlock.amountText,
            gainText = summaryBlock.gainText,
        )
        MainHomeBrokerBalanceRow(
            brokerageBalanceLine = summaryBlock.brokerageBalanceLine,
            onOpenBrokerAccount = onOpenBrokerAccount,
        )
        MainHomeOrdersRow(onOpenOrders = onOpenOrders)
        MainHomeHoldingsSection(holdings = holdings, onOpenSecurity = onOpenSecurity)
    }
}

@Composable
private fun MainHomePortfolioSummaryCard(amountText: String, gainText: String) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovSummaryCard {
        Text(
            stringResource(R.string.home_portfolio_value),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
        )
        Text(amountText, style = t.titleSemi22, color = c.onSurface)
        Text(
            stringResource(R.string.home_total_gain),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
        )
        Text(gainText, style = t.sectionSemi16, color = c.positive)
        Text(
            stringResource(R.string.home_loss_label),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.home_em_dash),
            style = t.sectionSemi16,
            color = c.onSurfaceVariant,
        )
    }
}

@Composable
private fun MainHomeOrdersRow(onOpenOrders: () -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovBalanceStrip(onClick = onOpenOrders) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ZovHalfUnit),
        ) {
            Text(
                stringResource(R.string.home_orders_strip_title),
                style = t.subtitleReg13,
                color = c.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.home_orders_strip_subtitle),
                style = t.titleSemi20,
                color = c.onSurface,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.cd_chevron),
            tint = c.onSurfaceVariant,
        )
    }
}

@Composable
private fun MainHomeBrokerBalanceRow(
    brokerageBalanceLine: String,
    onOpenBrokerAccount: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovBalanceStrip(onClick = onOpenBrokerAccount) {
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ZovHalfUnit),
        ) {
            Text(
                stringResource(R.string.home_brokerage_account),
                style = t.subtitleReg13,
                color = c.onSurfaceVariant,
            )
            Text(
                brokerageBalanceLine,
                style = t.titleSemi20,
                color = c.onSurface,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = stringResource(R.string.cd_chevron),
            tint = c.onSurfaceVariant,
        )
    }
}

@Composable
private fun MainHomeHoldingsSection(
    holdings: List<Holding>,
    onOpenSecurity: (securityId: String, displayTicker: String) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Text(stringResource(R.string.home_holdings), style = t.titleSemi20, color = c.onSurface)
    if (holdings.isNotEmpty()) {
        holdings.forEach { h ->
            AssetRow(
                AssetRowData(
                    subtitle = h.subtitle,
                    value = h.valueText,
                    delta = h.deltaText,
                    deltaPositive = h.deltaPositive,
                    ticker = h.ticker,
                ),
            ) { onOpenSecurity(h.detailNavKey, h.ticker) }
        }
    } else {
        Text(
            stringResource(R.string.home_no_positions),
            style = t.subtitleReg14,
            color = c.onSurfaceVariant,
        )
    }
}

internal data class AssetRowData(
    val subtitle: String,
    val value: String,
    val delta: String,
    val deltaPositive: Boolean,
    val ticker: String,
)

@Composable
internal fun AssetRow(
    data: AssetRowData,
    onClick: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovOutlinedRow(onClick = onClick) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ZovHalfUnit)) {
            Text(data.ticker, style = t.sectionSemi16, color = c.onSurface)
            Text(data.subtitle, style = t.labelReg12, color = c.onSurfaceVariant)
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(ZovHalfUnit),
        ) {
            Text(data.value, style = t.sectionSemi16, color = c.onSurface)
            Text(
                data.delta,
                style = t.bodyMed14,
                color = if (data.deltaPositive) c.positive else c.negative,
            )
        }
    }
}

@Composable
private fun SearchTabSearchField(
    value: String,
    onValueChange: (String) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ZovShapeMedium),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = c.outline,
            unfocusedBorderColor = c.outline,
            disabledBorderColor = c.outline,
            focusedContainerColor = c.surface,
            unfocusedContainerColor = c.surface,
            disabledContainerColor = c.surface,
            cursorColor = c.primary,
            focusedTextColor = c.onSurface,
            unfocusedTextColor = c.onSurface,
        ),
        placeholder = {
            Text(
                stringResource(R.string.search_placeholder),
                style = t.bodyReg14,
                color = c.onSurfaceVariant,
            )
        },
        singleLine = true,
    )
}

@Composable
internal fun SearchTabScreen(
    viewModel: ZovSearchTabViewModel,
    onOpenSecurity: (securityId: String, displayTicker: String) -> Unit,
) {
    val searchUi by viewModel.uiState.collectAsStateWithLifecycle()
    val chipResIds =
        listOf(
            R.string.filter_all,
            R.string.filter_stocks,
            R.string.filter_bonds,
            R.string.filter_etf,
        )
    ZovScrollScreen {
        SearchTabSearchField(value = searchUi.query, onValueChange = viewModel::setQuery)
        Row(horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
            chipResIds.forEachIndexed { i, resId ->
                ZovFilterChip(
                    label = stringResource(resId),
                    selected = searchUi.filterIndex == i,
                    onClick = { viewModel.setFilterIndex(i) },
                )
            }
        }
        SearchTabPopularSection(
            searchUi = searchUi,
            onOpenSecurity = onOpenSecurity,
            onLoadMore = viewModel::loadMore,
        )
    }
}

@Composable
internal fun HistoryTabScreen(viewModel: ZovHistoryTabViewModel) {
    val historyUi by viewModel.uiState.collectAsStateWithLifecycle()
    val filterResIds =
        listOf(
            R.string.history_filter_all,
            R.string.history_filter_buys,
            R.string.history_filter_sells,
            R.string.history_filter_deposits,
            R.string.history_filter_withdrawals,
        )
    ZovScrollScreen {
        HistoryFilterChipsRow(
            filterResIds = filterResIds,
            historyUi = historyUi,
            onSelectFilter = viewModel::setFilterIndex,
        )
        HistoryTabTransactionsSection(
            transactions = historyUi.transactions,
            historyUi = historyUi,
            onLoadMore = viewModel::loadMore,
        )
    }
}

@Preview(name = "Светлая", showBackground = true, locale = "ru")
@Composable
private fun MainHomePreviewLight() {
    ZovAppTheme(darkTheme = false) {
        MainHomeScrollContent(
            holdings = emptyList(),
            summaryBlock = MainHomeSummaryBlockTexts(
                amountText = "1 234 567,89 ₽",
                gainText = "+12 345,67 ₽ (+2,3%)",
                brokerageBalanceLine = "45 320,00 ₽",
            ),
            onOpenBrokerAccount = {},
            onOpenOrders = {},
            onOpenSecurity = { _, _ -> },
        )
    }
}

@Preview(name = "Тёмная", showBackground = true, locale = "ru")
@Composable
private fun MainHomePreviewDark() {
    ZovAppTheme(darkTheme = true) {
        MainHomeScrollContent(
            holdings = emptyList(),
            summaryBlock = MainHomeSummaryBlockTexts(
                amountText = "1 234 567,89 ₽",
                gainText = "+12 345,67 ₽ (+2,3%)",
                brokerageBalanceLine = "45 320,00 ₽",
            ),
            onOpenBrokerAccount = {},
            onOpenOrders = {},
            onOpenSecurity = { _, _ -> },
        )
    }
}
