package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
internal fun SecurityDetailOrderBookTab(detail: SecurityDetail) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Column(
        Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ZovItemSpacing),
    ) {
        Text(detail.priceLine, style = t.titleSemi22, color = c.onSurface)
        Text(
            detail.changeLine,
            style = t.bodyMed14,
            color = if (detail.changePositive) c.positive else c.negative,
        )
        val book = detail.orderBook
        if (book != null) {
            SecurityOrderBookPanel(orderBook = book, modifier = Modifier)
        } else {
            Text(
                detail.orderBookText ?: stringResource(R.string.security_order_book_placeholder),
                style = t.subtitleReg14,
                color = c.onSurfaceVariant,
            )
        }
    }
}
