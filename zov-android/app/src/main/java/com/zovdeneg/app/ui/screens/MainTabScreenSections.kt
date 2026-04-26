package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.market.SecurityListItem
import com.zovdeneg.app.domain.transactions.Transaction
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.ZovElevatedListCard
import com.zovdeneg.app.ui.components.ZovFilterChip
import com.zovdeneg.app.ui.tabs.HistoryTabUiState
import com.zovdeneg.app.ui.tabs.SearchTabUiState
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
internal fun SearchTabPopularSection(
    visibleSecurities: List<SecurityListItem>,
    searchUi: SearchTabUiState,
    onOpenSecurity: (String) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Text(stringResource(R.string.search_popular), style = t.sectionSemi16, color = c.onSurface)
    when {
        visibleSecurities.isNotEmpty() ->
            visibleSecurities.forEach { item ->
                AssetRow(
                    AssetRowData(
                        subtitle = item.subtitle,
                        value = item.valueText,
                        delta = item.deltaText,
                        deltaPositive = item.deltaPositive,
                        ticker = item.ticker,
                    ),
                ) { onOpenSecurity(item.detailNavKey) }
            }

        searchUi.loadFailed || (!searchUi.isLoading && searchUi.allSecurities.isEmpty()) -> {
            Text(
                stringResource(R.string.search_popular_empty),
                style = t.subtitleReg14,
                color = c.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun HistoryFilterChipsRow(
    filterResIds: List<Int>,
    historyUi: HistoryTabUiState,
    onSelectFilter: (Int) -> Unit,
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
    ) {
        filterResIds.forEachIndexed { i, resId ->
            ZovFilterChip(
                label = stringResource(resId),
                selected = historyUi.filterIndex == i,
                onClick = { onSelectFilter(i) },
            )
        }
    }
}

@Composable
internal fun HistoryTabTransactionsSection(
    visibleTxs: List<Transaction>,
    historyUi: HistoryTabUiState,
) {
    when {
        visibleTxs.isNotEmpty() ->
            visibleTxs.forEach { tx -> HistoryTransactionCard(tx = tx) }

        historyUi.loadFailed || (!historyUi.isLoading && historyUi.transactions.isEmpty()) ->
            HistoryTransactionsEmptyHint()
    }
}

@Composable
private fun HistoryTransactionCard(tx: Transaction) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val amountColor =
        when {
            tx.amountText.contains('-') -> c.negative
            tx.amountText.contains('+') -> c.positive
            else -> c.onSurface
        }
    ZovElevatedListCard {
        Text(tx.title, style = t.bodyMed14, color = c.onSurface)
        Text(tx.date, style = t.labelReg12, color = c.onSurfaceVariant)
        Spacer(Modifier.height(ZovUnit))
        Text(tx.amountText, style = t.sectionSemi16, color = amountColor)
    }
}

@Composable
private fun HistoryTransactionsEmptyHint() {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Text(
        stringResource(R.string.history_empty),
        style = t.subtitleReg14,
        color = c.onSurfaceVariant,
    )
}
