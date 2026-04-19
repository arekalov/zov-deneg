package com.zovdeneg.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovHalfUnit
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovShapeExtraSmall
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.ZovBalanceStrip
import com.zovdeneg.app.ui.components.ZovElevatedListCard
import com.zovdeneg.app.ui.components.ZovFilterChip
import com.zovdeneg.app.ui.components.ZovOutlinedRow
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.components.ZovSummaryCard
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

@Composable
fun MainHomeScreen(
    onOpenBrokerAccount: () -> Unit,
    onOpenSecurity: (String) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovScrollScreen {
        ZovSummaryCard {
            Text(stringResource(R.string.home_portfolio_value), style = t.subtitleReg13, color = c.onSurfaceVariant)
            Text(stringResource(R.string.home_portfolio_amount_mock), style = t.titleSemi22, color = c.onSurface)
            Text(stringResource(R.string.home_total_gain), style = t.subtitleReg13, color = c.onSurfaceVariant)
            Text(stringResource(R.string.home_total_gain_mock), style = t.sectionSemi16, color = c.positive)
            Text(stringResource(R.string.home_loss_label), style = t.subtitleReg13, color = c.onSurfaceVariant)
            Text(stringResource(R.string.home_em_dash), style = t.sectionSemi16, color = c.onSurfaceVariant)
        }
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
                    stringResource(R.string.home_brokerage_balance_mock),
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
        Text(stringResource(R.string.home_holdings), style = t.titleSemi20, color = c.onSurface)
        AssetRow(
            AssetRowData(
                stringResource(R.string.asset_sber_subtitle),
                stringResource(R.string.asset_sber_value),
                stringResource(R.string.asset_sber_delta),
                true,
                "SBER",
            ),
        ) { onOpenSecurity("SBER") }
        AssetRow(
            AssetRowData(
                stringResource(R.string.asset_lkoh_subtitle),
                stringResource(R.string.asset_lkoh_value),
                stringResource(R.string.asset_lkoh_delta),
                false,
                "LKOH",
            ),
        ) { onOpenSecurity("LKOH") }
    }
}

private data class AssetRowData(
    val subtitle: String,
    val value: String,
    val delta: String,
    val deltaPositive: Boolean,
    val ticker: String,
)

@Composable
private fun AssetRow(
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
private fun SearchTabSearchField() {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    OutlinedTextField(
        value = "",
        onValueChange = {},
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ZovShapeExtraSmall),
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
            Text(stringResource(R.string.search_placeholder), style = t.bodyReg14, color = c.onSurfaceVariant)
        },
        singleLine = true,
    )
}

@Composable
fun SearchTabScreen(onOpenSecurity: (String) -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var filter by remember { mutableIntStateOf(0) }
    val chipResIds =
        listOf(
            R.string.filter_all,
            R.string.filter_stocks,
            R.string.filter_bonds,
            R.string.filter_etf,
        )
    ZovScrollScreen {
        SearchTabSearchField()
        Row(horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
            chipResIds.forEachIndexed { i, resId ->
                ZovFilterChip(
                    label = stringResource(resId),
                    selected = filter == i,
                    onClick = { filter = i },
                )
            }
        }
        Text(stringResource(R.string.search_popular), style = t.sectionSemi16, color = c.onSurface)
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

@Composable
fun HistoryTabScreen() {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var filter by remember { mutableIntStateOf(0) }
    val txs =
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
    val filterResIds =
        listOf(
            R.string.history_filter_all,
            R.string.history_filter_buys,
            R.string.history_filter_sells,
        )
    ZovScrollScreen {
        Row(horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
            filterResIds.forEachIndexed { i, resId ->
                ZovFilterChip(
                    label = stringResource(resId),
                    selected = filter == i,
                    onClick = { filter = i },
                )
            }
        }
        txs.forEach { triple ->
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
}

@Preview(name = "Светлая", showBackground = true, locale = "ru")
@Composable
private fun MainHomePreviewLight() {
    ZovAppTheme(darkTheme = false) {
        MainHomeScreen({}, {})
    }
}

@Preview(name = "Тёмная", showBackground = true, locale = "ru")
@Composable
private fun MainHomePreviewDark() {
    ZovAppTheme(darkTheme = true) {
        MainHomeScreen({}, {})
    }
}
