package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.market.PriceHistoryPoint
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.components.ZovCenteredCircularProgress
import com.zovdeneg.app.ui.components.ZovChartPeriodChip
import com.zovdeneg.app.ui.components.ZovSummaryCard
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

@Composable
private fun SecurityDetailPortfolioQuantityValueRow(
    qty: Int,
    totalValue: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
    ) {
        SecurityDetailAboutStatCell(
            label = stringResource(R.string.security_portfolio_qty_label),
            value = stringResource(R.string.security_lot_value_format, qty),
            modifier = Modifier.weight(1f),
        )
        SecurityDetailAboutStatCell(
            label = stringResource(R.string.security_portfolio_total_value_label),
            value = totalValue,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SecurityDetailPortfolioUnitAvgRow(
    unitPrice: String,
    avgLine: String,
    dash: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
    ) {
        SecurityDetailAboutStatCell(
            label = stringResource(R.string.security_portfolio_unit_price_label),
            value = unitPrice,
            modifier = Modifier.weight(1f),
        )
        SecurityDetailAboutStatCell(
            label = stringResource(R.string.security_portfolio_avg_price_label),
            value = avgLine.ifBlank { dash },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SecurityDetailPortfolioDeltasColumn(
    detail: SecurityDetail,
    positionDelta: String?,
    positionDeltaColor: Color,
    dash: String,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Text(
        stringResource(R.string.security_portfolio_unit_day_change_label),
        style = t.labelReg12,
        color = c.onSurfaceVariant,
    )
    Text(
        detail.changeLine,
        style = t.bodyMed14,
        color = if (detail.changePositive) c.positive else c.negative,
    )
    Text(
        stringResource(R.string.security_portfolio_position_result_label),
        style = t.labelReg12,
        color = c.onSurfaceVariant,
    )
    Text(
        text = positionDelta ?: dash,
        style = t.bodyMed14,
        color = if (positionDelta != null) positionDeltaColor else c.onSurfaceVariant,
    )
}

@Composable
internal fun SecurityDetailPeriodChipsRow(
    chartRange: SecurityChartRange,
    onSelectChartRange: (SecurityChartRange) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZovTightGap),
    ) {
        SecurityChartRange.entries.forEach { range ->
            val label =
                when (range) {
                    SecurityChartRange.ONE_DAY -> stringResource(R.string.security_chart_period_1d)
                    SecurityChartRange.ONE_WEEK -> stringResource(R.string.security_chart_period_1w)
                    SecurityChartRange.ONE_MONTH -> stringResource(R.string.security_chart_period_1m)
                    SecurityChartRange.ONE_YEAR -> stringResource(R.string.security_chart_period_1y)
                }
            ZovChartPeriodChip(
                label = label,
                selected = chartRange == range,
                onClick = { onSelectChartRange(range) },
            )
        }
    }
}

@Composable
internal fun SecurityDetailPortfolioBanner(
    detail: SecurityDetail,
    onSell: () -> Unit,
) {
    val qty = detail.portfolioQuantity ?: return
    if (qty < 1) return
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val dash = stringResource(R.string.home_em_dash)
    val totalValue = detail.portfolioCurrentValueLine?.takeIf { it.isNotBlank() } ?: dash
    val unitPrice = detail.portfolioUnitPriceLine?.takeIf { it.isNotBlank() } ?: detail.priceLine
    val avgLine = detail.portfolioAvgPriceLine?.takeIf { it.isNotBlank() }.orEmpty()
    val positionDelta = detail.portfolioPositionDeltaLine?.takeIf { it.isNotBlank() }
    val positionDeltaColor =
        when (detail.portfolioPositionDeltaPositive) {
            true -> c.positive
            false -> c.negative
            null -> c.onSurfaceVariant
        }
    ZovSummaryCard {
        Text(
            stringResource(R.string.security_portfolio_card_title),
            style = t.sectionSemi16,
            color = c.onSurface,
        )
        SecurityDetailPortfolioQuantityValueRow(qty = qty, totalValue = totalValue)
        SecurityDetailPortfolioUnitAvgRow(unitPrice = unitPrice, avgLine = avgLine, dash = dash)
        SecurityDetailPortfolioDeltasColumn(
            detail = detail,
            positionDelta = positionDelta,
            positionDeltaColor = positionDeltaColor,
            dash = dash,
        )
        if (detail.canSellAtLeastOneLot) {
            Spacer(Modifier.height(ZovItemSpacing))
            OutlinedButton(
                onClick = onSell,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = c.negative),
            ) {
                Text(stringResource(R.string.action_sell), style = t.bodyMed14)
            }
        }
    }
}

@Composable
internal fun SecurityDetailAboutStatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Column(modifier.fillMaxWidth()) {
        Text(label, style = t.labelReg12, color = c.onSurfaceVariant)
        Text(value, style = t.bodyMed14, color = c.onSurface)
    }
}

@Composable
internal fun SecurityDetailAboutCompanyCard(detail: SecurityDetail) {
    val dash = stringResource(R.string.home_em_dash)
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val sector = detail.sectorName.ifBlank { dash }
    val exchange = detail.exchangeCode.ifBlank { dash }
    val lot = stringResource(R.string.security_lot_value_format, detail.lotSize)
    ZovSummaryCard {
        Text(
            stringResource(R.string.security_about_company_title),
            style = t.sectionSemi16,
            color = c.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
        ) {
            SecurityDetailAboutStatCell(
                label = stringResource(R.string.security_sector_label),
                value = sector,
                modifier = Modifier.weight(1f),
            )
            SecurityDetailAboutStatCell(
                label = stringResource(R.string.security_exchange_label),
                value = exchange,
                modifier = Modifier.weight(1f),
            )
            SecurityDetailAboutStatCell(
                label = stringResource(R.string.security_lot_label),
                value = lot,
                modifier = Modifier.weight(1f),
            )
        }
        val desc = detail.companyDescription?.trim().orEmpty()
        if (desc.isNotEmpty()) {
            Text(desc, style = t.bodyReg14, color = c.onSurfaceVariant)
        }
    }
}

@Composable
internal fun SecurityDetailPriceChartBlock(
    chartLoading: Boolean,
    chartFailed: Boolean,
    priceHistory: List<PriceHistoryPoint>,
    chartRange: SecurityChartRange,
    onSelectChartRange: (SecurityChartRange) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
        Text(
            stringResource(R.string.security_price_chart_section_title),
            style = t.subtitleReg13,
            color = c.onSurface,
            modifier = Modifier.padding(top = ZovItemSpacing),
        )
        val hasChartContent = priceHistory.isNotEmpty()
        val showPeriodChips = chartFailed || chartLoading || hasChartContent
        when {
            chartFailed ->
                Text(
                    stringResource(R.string.security_chart_load_failed),
                    style = t.bodyReg14,
                    color = c.negative,
                )
            hasChartContent -> {
                ZovSecurityPriceChart(
                    points = priceHistory,
                    chartRange = chartRange,
                )
            }
            chartLoading -> {
                ZovCenteredCircularProgress()
            }
            else ->
                Text(
                    stringResource(R.string.security_chart_empty),
                    style = t.subtitleReg13,
                    color = c.onSurfaceVariant,
                )
        }
        if (showPeriodChips) {
            SecurityDetailPeriodChipsRow(
                chartRange = chartRange,
                onSelectChartRange = onSelectChartRange,
            )
        }
        HorizontalDivider(color = c.outline)
    }
}
