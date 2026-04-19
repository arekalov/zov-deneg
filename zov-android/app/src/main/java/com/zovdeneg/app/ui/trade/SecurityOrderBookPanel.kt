package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.market.SecurityOrderBook
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val ASK_BLOCK_ALPHA = 0.09f
private const val ASK_BEST_ROW_ALPHA = 0.16f
private const val BID_BLOCK_ALPHA = 0.09f
private const val BID_BEST_ROW_ALPHA = 0.16f

internal fun formatOrderBookVolume(value: Int): String =
    value.toString().reversed().chunked(3).joinToString(" ").reversed()

internal fun formatOrderBookPriceLine(decimalRaw: String): String {
    val normalized = decimalRaw.trim().replace(',', '.')
    val parts = normalized.split('.')
    val intPart = parts.firstOrNull()?.ifEmpty { "0" } ?: "0"
    val frac = (parts.getOrNull(1) ?: "00").take(2).padEnd(2, '0')
    return "$intPart,$frac ₽"
}

@Composable
internal fun SecurityOrderBookPanel(
    orderBook: SecurityOrderBook,
    modifier: Modifier = Modifier,
) {
    val c = ZovTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ZovShapeMedium),
        color = c.surface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(Modifier.fillMaxWidth()) {
            OrderBookHeaderRow()
            HorizontalDivider(color = c.outline)
            OrderBookAsksBlock(orderBook = orderBook)
            HorizontalDivider(color = c.outline)
            OrderBookBidsBlock(orderBook = orderBook)
        }
    }
}

@Composable
private fun OrderBookAsksBlock(orderBook: SecurityOrderBook) {
    val c = ZovTheme.colors
    val askBlock = c.negative.copy(alpha = ASK_BLOCK_ALPHA)
    val askBest = c.negative.copy(alpha = ASK_BEST_ROW_ALPHA)
    Column(
        Modifier
            .fillMaxWidth()
            .background(askBlock),
    ) {
        orderBook.askLevels.forEach { row ->
            OrderBookDataRow(
                leftText = formatOrderBookVolume(row.leftQuantity),
                priceText = formatOrderBookPriceLine(row.priceDecimal),
                rightText = row.rightQuantity?.let { formatOrderBookVolume(it) },
                priceColor = c.negative,
            )
        }
        OrderBookBestRow(
            label = stringResource(R.string.security_order_book_best_ask),
            priceText = formatOrderBookPriceLine(orderBook.bestAskPriceDecimal),
            background = askBest,
            priceColor = c.negative,
        )
    }
}

@Composable
private fun OrderBookBidsBlock(orderBook: SecurityOrderBook) {
    val c = ZovTheme.colors
    val bidBlock = c.positive.copy(alpha = BID_BLOCK_ALPHA)
    val bidBest = c.positive.copy(alpha = BID_BEST_ROW_ALPHA)
    Column(
        Modifier
            .fillMaxWidth()
            .background(bidBlock),
    ) {
        OrderBookBestRow(
            label = stringResource(R.string.security_order_book_best_bid),
            priceText = formatOrderBookPriceLine(orderBook.bestBidPriceDecimal),
            background = bidBest,
            priceColor = c.positive,
        )
        orderBook.bidLevels.forEach { row ->
            OrderBookDataRow(
                leftText = formatOrderBookVolume(row.leftQuantity),
                priceText = formatOrderBookPriceLine(row.priceDecimal),
                rightText = row.rightQuantity?.let { formatOrderBookVolume(it) },
                priceColor = c.positive,
            )
        }
    }
}

@Composable
private fun OrderBookHeaderRow() {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = ZovHorizontalPadding, vertical = ZovTightGap),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.security_order_book_col_volume),
            Modifier.weight(1f),
            style = t.labelReg12,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Start,
        )
        Text(
            stringResource(R.string.security_order_book_col_price),
            Modifier.weight(1f),
            style = t.labelReg12,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Text(
            stringResource(R.string.security_order_book_col_volume),
            Modifier.weight(1f),
            style = t.labelReg12,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun OrderBookDataRow(
    leftText: String,
    priceText: String,
    rightText: String?,
    priceColor: Color,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = ZovHorizontalPadding, vertical = ZovTightGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            leftText,
            Modifier.weight(1f),
            style = t.bodyReg14,
            color = c.onSurface,
            textAlign = TextAlign.Start,
        )
        Text(
            priceText,
            Modifier.weight(1f),
            style = t.bodyMed14,
            color = priceColor,
            textAlign = TextAlign.Center,
        )
        Text(
            rightText.orEmpty(),
            Modifier.weight(1f),
            style = t.bodyReg14,
            color = c.onSurface,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun OrderBookBestRow(
    label: String,
    priceText: String,
    background: Color,
    priceColor: Color,
) {
    val t = ZovTheme.text
    Row(
        Modifier
            .fillMaxWidth()
            .background(background)
            .padding(horizontal = ZovHorizontalPadding, vertical = ZovTightGap),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = t.bodyMed14, color = priceColor)
        Text(priceText, style = t.bodyMed14, color = priceColor)
    }
}
