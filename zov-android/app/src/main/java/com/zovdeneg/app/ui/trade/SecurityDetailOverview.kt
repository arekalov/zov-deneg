package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.market.PriceHistoryPoint
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.common.ZovSpace3
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.components.ZovChartPeriodChip
import com.zovdeneg.app.ui.components.ZovSummaryCard
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

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
internal fun SecurityDetailPortfolioBanner(detail: SecurityDetail) {
    val qty = detail.portfolioQuantity ?: return
    val avg = detail.portfolioAvgPriceLine ?: return
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(ZovShapeMedium))
            .background(c.primaryContainer)
            .padding(vertical = ZovSpace3, horizontal = ZovHorizontalPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            stringResource(R.string.security_portfolio_badge, qty, avg),
            style = t.bodyMed14,
            color = c.positive,
            textAlign = TextAlign.Center,
        )
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
        when {
            chartFailed ->
                Text(
                    stringResource(R.string.security_chart_load_failed),
                    style = t.bodyReg14,
                    color = c.negative,
                )
            priceHistory.isNotEmpty() -> {
                ZovSecurityPriceChart(
                    points = priceHistory,
                    chartRange = chartRange,
                )
            }
            chartLoading -> {
                Spacer(Modifier.height(ZovTightGap))
                CircularProgressIndicator(color = c.primary)
            }
            else ->
                Text(
                    stringResource(R.string.security_chart_empty),
                    style = t.subtitleReg13,
                    color = c.onSurfaceVariant,
                )
        }
        SecurityDetailPeriodChipsRow(
            chartRange = chartRange,
            onSelectChartRange = onSelectChartRange,
        )
        HorizontalDivider(color = c.outline)
    }
}
