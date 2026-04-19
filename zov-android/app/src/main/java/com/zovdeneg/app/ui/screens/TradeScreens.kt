package com.zovdeneg.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovFieldInnerPadding
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.components.ZovFilterChip
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

@Composable
fun SecurityDetailScreen(
    ticker: String,
    onBuy: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var tab by remember { mutableIntStateOf(0) }
    val label = ticker.replace('_', '/')
    ZovScrollScreen {
        TabRow(selectedTabIndex = tab) {
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
        if (tab == 0) {
            Text(
                stringResource(R.string.security_subtitle_format, label),
                style = t.labelReg12,
                color = c.onSurfaceVariant,
            )
            Text(stringResource(R.string.security_price_mock), style = t.titleSemi22, color = c.onSurface)
            Text(stringResource(R.string.security_change_mock), style = t.bodyMed14, color = c.positive)
            Text(
                stringResource(R.string.security_chart_placeholder),
                style = t.subtitleReg13,
                color = c.onSurfaceVariant,
            )
        } else {
            Text(
                stringResource(R.string.security_order_book_placeholder),
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

@Composable
fun BuyScreen(
    ticker: String,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val label = ticker.replace('_', '/')
    ZovScrollScreen {
        Text(stringResource(R.string.buy_title_format, label), style = t.sectionSemi16, color = c.onSurface)
        Text(stringResource(R.string.buy_lots_label), style = t.labelMed12, color = c.onSurfaceVariant)
        Text(
            stringResource(R.string.buy_lots_mock),
            style = t.pinAmount20,
            modifier = Modifier
                .fillMaxWidth()
                .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
                .padding(ZovFieldInnerPadding),
        )
        Text(stringResource(R.string.buy_order_value_mock), style = t.subtitleReg14, color = c.onSurfaceVariant)
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
        ) { Text(stringResource(R.string.action_place_order), style = t.bodyMed14) }
    }
}

@Composable
private fun DepositTabContent(
    chipResIds: List<Int>,
    amountChip: Int,
    onAmountChip: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
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
    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.deposit_action))
    }
}

@Composable
private fun WithdrawTabContent(onBack: () -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Text(stringResource(R.string.withdraw_amount_label), style = t.bodyMed14, color = c.onSurface)
    Text(
        stringResource(R.string.withdraw_amount_mock),
        style = t.pinAmount20,
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
            .padding(ZovHorizontalPadding),
    )
    Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.withdraw_action))
    }
}

@Composable
fun DepositScreen(onBack: () -> Unit) {
    val t = ZovTheme.text
    var tab by remember { mutableIntStateOf(0) }
    var amountChip by remember { mutableIntStateOf(0) }
    val chipResIds =
        listOf(
            R.string.deposit_chip_1000,
            R.string.deposit_chip_5000,
            R.string.deposit_chip_10000,
            R.string.deposit_chip_50000,
        )
    ZovScrollScreen {
        TabRow(selectedTabIndex = tab) {
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
        if (tab == 0) {
            DepositTabContent(
                chipResIds = chipResIds,
                amountChip = amountChip,
                onAmountChip = { amountChip = it },
                onBack = onBack,
            )
        } else {
            WithdrawTabContent(onBack = onBack)
        }
    }
}

@Preview(name = "Светлая", showBackground = true, locale = "ru")
@Composable
private fun DetailPreviewLight() {
    ZovAppTheme(darkTheme = false) { SecurityDetailScreen("SBER", {}) }
}

@Preview(name = "Тёмная", showBackground = true, locale = "ru")
@Composable
private fun DetailPreviewDark() {
    ZovAppTheme(darkTheme = true) { SecurityDetailScreen("SBER", {}) }
}
