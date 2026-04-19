package com.zovdeneg.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
                ) { onOpenSecurity(item.ticker) }
            }
        searchUi.loadFailed || (!searchUi.isLoading && searchUi.allSecurities.isEmpty()) -> {
            AssetRow(
                AssetRowData(
                    stringResource(R.string.asset_gazp_subtitle),
                    stringResource(R.string.asset_gazp_value),
                    stringResource(R.string.asset_gazp_delta),
                    true,
                    "GAZP",
                ),
            ) { onOpenSecurity("GAZP") }
            AssetRow(
                AssetRowData(
                    stringResource(R.string.asset_sber_search_subtitle),
                    stringResource(R.string.asset_sber_search_value),
                    stringResource(R.string.asset_sber_search_delta),
                    true,
                    "SBER",
                ),
            ) { onOpenSecurity("SBER") }
        }
    }
}

@Composable
internal fun HistoryFilterChipsRow(
    filterResIds: List<Int>,
    historyUi: HistoryTabUiState,
    onSelectFilter: (Int) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
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
            HistoryTransactionFallbackRows()
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
private fun HistoryTransactionFallbackRows() {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val fallback =
        listOf(
            Triple(
                R.string.history_row1_title,
                R.string.history_row1_date,
                R.string.history_row1_amount,
            ),
            Triple(
                R.string.history_row2_title,
                R.string.history_row2_date,
                R.string.history_row2_amount,
            ),
        )
    fallback.forEach { triple ->
        val title = stringResource(triple.first)
        val date = stringResource(triple.second)
        val amount = stringResource(triple.third)
        val amountColor =
            when {
                amount.contains('-') -> c.negative
                amount.contains('+') -> c.positive
                else -> c.onSurface
            }
        ZovElevatedListCard {
            Text(title, style = t.bodyMed14, color = c.onSurface)
            Text(date, style = t.labelReg12, color = c.onSurfaceVariant)
            Spacer(Modifier.height(ZovUnit))
            Text(amount, style = t.sectionSemi16, color = amountColor)
        }
    }
}
