package com.zovdeneg.app.ui.components

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovButtonHeight
import com.zovdeneg.app.ui.common.ZovCardElevation
import com.zovdeneg.app.ui.common.ZovChipHeight
import com.zovdeneg.app.ui.common.ZovHalfUnit
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovListRowMinHeight
import com.zovdeneg.app.ui.common.ZovPinDotSize
import com.zovdeneg.app.ui.common.ZovPinDotSpacing
import com.zovdeneg.app.ui.common.ZovPinKeyRowHeight
import com.zovdeneg.app.ui.common.ZovScrollBodySpacing
import com.zovdeneg.app.ui.common.ZovShapeLarge
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.common.ZovShapeSmall
import com.zovdeneg.app.ui.common.ZovSpace3
import com.zovdeneg.app.ui.common.ZovSpace6
import com.zovdeneg.app.ui.common.ZovSpace8
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ZovSummaryCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = ZovTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ZovShapeMedium),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = ZovCardElevation),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ZovHorizontalPadding, vertical = ZovSpace3),
            verticalArrangement = Arrangement.spacedBy(ZovTightGap),
            content = content,
        )
    }
}

@Composable
fun ZovElevatedListCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = ZovTheme.colors
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ZovShapeMedium),
        colors = CardDefaults.cardColors(containerColor = c.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = ZovCardElevation),
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = ZovHorizontalPadding, vertical = ZovSpace3),
            verticalArrangement = Arrangement.spacedBy(ZovUnit),
            content = content,
        )
    }
}

@Composable
fun ZovOutlinedRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val c = ZovTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = ZovListRowMinHeight)
            .clip(RoundedCornerShape(ZovShapeMedium))
            .border(1.dp, c.outline, RoundedCornerShape(ZovShapeMedium))
            .background(c.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = ZovHorizontalPadding, vertical = ZovSpace3),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ZovHorizontalPadding),
        content = content,
    )
}

@Composable
fun ZovBalanceStrip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val c = ZovTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ZovShapeMedium))
            .border(1.dp, c.outline, RoundedCornerShape(ZovShapeMedium))
            .background(c.surface)
            .clickable(onClick = onClick)
            .padding(
                start = ZovHorizontalPadding,
                end = ZovSpace3,
                top = ZovSpace3,
                bottom = ZovSpace3,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
        content = content,
    )
}

@Composable
fun ZovFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val shape = RoundedCornerShape(ZovShapeSmall)
    Box(
        modifier
            .height(ZovChipHeight)
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.background(c.primaryContainer)
                } else {
                    Modifier
                        .background(c.surfaceContainer)
                        .border(1.dp, c.outline, shape)
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = ZovHorizontalPadding, vertical = ZovTightGap),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = t.bodyMed14,
            color = if (selected) c.positive else c.onSurfaceVariant,
        )
    }
}

@Composable
fun ZovPinDots(
    filledCount: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val c = ZovTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .then(modifier)
            .padding(vertical = ZovItemSpacing),
        horizontalArrangement = Arrangement.spacedBy(
            ZovPinDotSpacing,
            Alignment.CenterHorizontally,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { i ->
            val filled = i < filledCount
            Box(
                Modifier
                    .size(ZovPinDotSize)
                    .clip(CircleShape)
                    .background(if (filled) c.primary else c.surface)
                    .then(
                        if (!filled) {
                            Modifier.border(ZovHalfUnit, c.outline, CircleShape)
                        } else {
                            Modifier
                        },
                    ),
            )
        }
    }
}

private val pinRowResIds: List<List<Int>> =
    listOf(
        listOf(R.string.pin_key_1, R.string.pin_key_2, R.string.pin_key_3),
        listOf(R.string.pin_key_4, R.string.pin_key_5, R.string.pin_key_6),
        listOf(R.string.pin_key_7, R.string.pin_key_8, R.string.pin_key_9),
        listOf(R.string.pin_key_delete, R.string.pin_key_0, R.string.pin_key_ok),
    )

@Composable
private fun RowScope.ZovPinKeyCell(
    resId: Int,
    onDigit: (Int) -> Unit,
    onDelete: () -> Unit,
    onConfirm: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val label = stringResource(resId)
    val isOk = resId == R.string.pin_key_ok
    val isDel = resId == R.string.pin_key_delete
    val digit = label.toIntOrNull()
    val containerColor =
        when {
            isOk -> c.primary
            isDel -> c.surfaceContainer
            else -> c.surface
        }
    val labelStyle =
        when {
            isOk -> t.sectionSemi16
            else -> t.titleSemi20
        }
    val labelColor =
        when {
            isOk -> c.onPrimary
            isDel -> c.onSurfaceVariant
            else -> c.onSurface
        }
    Box(
        Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(ZovShapeLarge))
            .background(containerColor)
            .clickable {
                when {
                    isOk -> onConfirm()
                    isDel -> onDelete()
                    digit != null -> onDigit(digit)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = labelStyle,
            color = labelColor,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun ZovPinKeypad(
    modifier: Modifier = Modifier,
    onDigit: (Int) -> Unit = {},
    onDelete: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {
    Column(
        modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ZovItemSpacing),
    ) {
        pinRowResIds.forEach { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(ZovPinKeyRowHeight),
                horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
            ) {
                row.forEach { resId ->
                    ZovPinKeyCell(
                        resId = resId,
                        onDigit = onDigit,
                        onDelete = onDelete,
                        onConfirm = onConfirm,
                    )
                }
            }
        }
    }
}

@Composable
fun ZovBiometricFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val c = ZovTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .height(ZovButtonHeight)
            .clip(RoundedCornerShape(ZovSpace8))
            .background(c.primaryContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = ZovSpace6),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun ZovM3PreviewScrollColumn(content: @Composable ColumnScope.() -> Unit) {
    val c = ZovTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .background(c.background)
            .padding(ZovHorizontalPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(ZovScrollBodySpacing),
        content = content,
    )
}

@Composable
private fun ZovM3PreviewCardsBlock() {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovSummaryCard {
        Text(
            stringResource(R.string.home_portfolio_value),
            style = t.labelReg12,
            color = c.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.home_portfolio_amount_mock),
            style = t.titleSemi22,
            color = c.onSurface,
        )
    }
    ZovBalanceStrip(onClick = {}) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ZovHalfUnit)) {
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
    }
    ZovOutlinedRow(onClick = {}) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(ZovHalfUnit)) {
            Text("SBER", style = t.sectionSemi16, color = c.onSurface)
            Text(
                stringResource(R.string.asset_sber_subtitle),
                style = t.labelReg12,
                color = c.onSurfaceVariant,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(ZovHalfUnit),
        ) {
            Text(
                stringResource(R.string.asset_sber_value),
                style = t.sectionSemi16,
                color = c.onSurface,
            )
            Text(stringResource(R.string.asset_sber_delta), style = t.bodyMed14, color = c.positive)
        }
    }
    ZovElevatedListCard {
        Text(stringResource(R.string.history_row1_title), style = t.bodyMed14, color = c.onSurface)
        Text(
            stringResource(R.string.history_row1_date),
            style = t.labelReg12,
            color = c.onSurfaceVariant,
        )
    }
}

@Composable
private fun ZovM3PreviewControlsBlock(showKeypadAndBiometric: Boolean) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Row(horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
        ZovFilterChip(stringResource(R.string.filter_all), selected = true, onClick = {})
        ZovFilterChip(stringResource(R.string.filter_stocks), selected = false, onClick = {})
    }
    ZovPinDots(filledCount = 2, total = 4)
    if (showKeypadAndBiometric) {
        ZovPinKeypad(modifier = Modifier.fillMaxWidth())
        ZovBiometricFilledButton(onClick = {}) {
            Icon(
                Icons.Filled.Fingerprint,
                contentDescription = stringResource(R.string.cd_fingerprint),
                tint = c.primary,
            )
            Text(
                stringResource(R.string.auth_sign_in_biometrics),
                style = t.bodyMed14,
                color = c.primary,
            )
        }
    }
}

@Preview(name = "Компоненты · светлая", showBackground = true, locale = "ru", heightDp = 900)
@Composable
private fun ZovM3ComponentsPreviewLight() {
    ZovAppTheme(darkTheme = false) {
        ZovM3PreviewScrollColumn {
            ZovM3PreviewCardsBlock()
            ZovM3PreviewControlsBlock(showKeypadAndBiometric = true)
        }
    }
}

@Preview(name = "Компоненты · тёмная", showBackground = true, locale = "ru", heightDp = 480)
@Composable
private fun ZovM3ComponentsPreviewDark() {
    ZovAppTheme(darkTheme = true) {
        ZovM3PreviewScrollColumn {
            ZovM3PreviewCardsBlock()
            Row(horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
                ZovFilterChip(stringResource(R.string.filter_bonds), selected = false, onClick = {})
                ZovFilterChip(stringResource(R.string.filter_etf), selected = true, onClick = {})
            }
            ZovPinDots(filledCount = 4, total = 4)
        }
    }
}
