package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.market.SecurityKind
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
internal fun SearchTabPopularSection(
    searchUi: SearchTabUiState,
    onOpenSecurity: (securityId: String, displayTicker: String) -> Unit,
    onLoadMore: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Text(stringResource(R.string.search_popular), style = t.sectionSemi16, color = c.onSurface)
    when {
        searchUi.isLoading && searchUi.securities.isEmpty() ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = c.primary,
                    strokeWidth = 2.dp,
                )
            }

        searchUi.securities.isNotEmpty() -> {
            searchUi.securities.forEach { item ->
                AssetRow(
                    AssetRowData(
                        subtitle = item.subtitle,
                        value = item.valueText,
                        delta = item.deltaText,
                        deltaPositive = item.deltaPositive,
                        ticker = item.ticker,
                        isBond = item.kind == SecurityKind.BOND,
                    ),
                ) { onOpenSecurity(item.detailNavKey, item.ticker) }
            }
            ListLoadMoreFooter(
                hasMore = searchUi.hasMore,
                isLoadingMore = searchUi.isLoadingMore,
                onLoadMore = onLoadMore,
            )
        }

        searchUi.loadFailed || (!searchUi.isLoading && searchUi.securities.isEmpty()) -> {
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
    transactions: List<Transaction>,
    historyUi: HistoryTabUiState,
    onLoadMore: () -> Unit,
) {
    when {
        historyUi.isLoading && transactions.isEmpty() ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                val c = ZovTheme.colors
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = c.primary,
                    strokeWidth = 2.dp,
                )
            }

        transactions.isNotEmpty() -> {
            transactions.forEach { tx -> HistoryTransactionCard(tx = tx) }
            ListLoadMoreFooter(
                hasMore = historyUi.hasMore,
                isLoadingMore = historyUi.isLoadingMore,
                onLoadMore = onLoadMore,
            )
        }

        historyUi.loadFailed || (!historyUi.isLoading && transactions.isEmpty()) ->
            HistoryTransactionsEmptyHint()
    }
}

@Composable
private fun ListLoadMoreFooter(
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
) {
    if (!hasMore) return
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Spacer(Modifier.height(ZovItemSpacing))
    if (isLoadingMore) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = c.primary,
                strokeWidth = 2.dp,
            )
        }
    } else {
        OutlinedButton(
            onClick = onLoadMore,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.list_load_more), style = t.bodyMed14)
        }
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
