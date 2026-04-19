package com.zovdeneg.app.ui.trade

import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.Animation
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.line.AreaChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineData
import com.zovdeneg.app.domain.market.PriceHistoryPoint
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val chartTimeHourMinute: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Europe/Moscow"))
private val chartTimeDayMonth: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd.MM").withZone(ZoneId.of("Europe/Moscow"))

@OptIn(ExperimentalTextApi::class)
@Composable
fun ZovSecurityPriceChart(
    points: List<PriceHistoryPoint>,
    chartRange: SecurityChartRange,
    modifier: Modifier = Modifier,
) {
    val c = ZovTheme.colors
    val firstTs = points.firstOrNull()?.timestampSeconds ?: 0L
    val lastTs = points.lastOrNull()?.timestampSeconds ?: 0L
    key(chartRange, points.size, firstTs, lastTs) {
        val lineData = remember(points) { points.toSortedPriceLineData() }
        val lineColor = c.primary
        val areaGradient =
            ChartyColor.Gradient(
                listOf(
                    lineColor.copy(alpha = 0.42f),
                    lineColor.copy(alpha = 0.06f),
                ),
            )
        val labelStyle = TextStyle(color = c.onSurfaceVariant, fontSize = 9.sp)
        val scaffoldConfig =
            ChartScaffoldConfig(
                showGrid = false,
                showAxis = false,
                showLabels = true,
                axisColor = Color.Transparent,
                gridColor = Color.Transparent,
                labelTextStyle = labelStyle,
            )
        val lineConfig =
            LineChartConfig(
                smoothCurve = true,
                showPoints = false,
                lineWidth = 2.5f,
                animation = Animation.Disabled,
            )
        val chartModifier = modifier.padding(horizontal = 6.dp).height(200.dp).fillMaxWidth()
        AreaChart(
            data = { lineData },
            modifier = chartModifier,
            color = areaGradient,
            lineConfig = lineConfig,
            scaffoldConfig = scaffoldConfig,
            fillAlpha = 0.38f,
            onPointClick = null,
        )
    }
}

private fun List<PriceHistoryPoint>.toSortedPriceLineData(): List<LineData> {
    if (isEmpty()) return emptyList()
    val sorted = sortedBy { it.timestampSeconds }
    val n = sorted.size
    val labelAt = labelIndices(n, maxLabels = 5)
    val spanSec = (sorted.last().timestampSeconds - sorted.first().timestampSeconds).coerceAtLeast(1L)
    val fmt = if (spanSec <= 36 * 3600) chartTimeHourMinute else chartTimeDayMonth
    return sorted.mapIndexed { index, p ->
        val label =
            if (index in labelAt) {
                fmt.format(Instant.ofEpochSecond(p.timestampSeconds))
            } else {
                ""
            }
        LineData(label = label, value = p.price.toFloat())
    }
}

private fun labelIndices(count: Int, maxLabels: Int): Set<Int> {
    if (count <= 0) return emptySet()
    if (count == 1) return setOf(0)
    val k = maxLabels.coerceAtLeast(2)
    val out = LinkedHashSet<Int>()
    out.add(0)
    for (i in 1 until k - 1) {
        out.add((i * (count - 1) / (k - 1)).coerceIn(0, count - 1))
    }
    out.add(count - 1)
    return out
}
