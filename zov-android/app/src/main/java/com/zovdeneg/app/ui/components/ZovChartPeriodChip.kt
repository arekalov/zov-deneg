package com.zovdeneg.app.ui.components

import com.zovdeneg.app.ui.common.ZovChipHeight
import com.zovdeneg.app.ui.common.ZovSpace4
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip

@Composable
fun ZovChartPeriodChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val pill = RoundedCornerShape(percent = 50)
    Box(
        modifier
            .heightIn(min = ZovChipHeight)
            .then(
                if (selected) {
                    Modifier
                        .height(ZovChipHeight)
                        .clip(pill)
                        .background(c.primaryContainer)
                } else {
                    Modifier
                },
            )
            .clickable(onClick = onClick)
            .padding(horizontal = ZovSpace4, vertical = ZovTightGap),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = t.bodyMed14,
            color = if (selected) c.positive else c.onSurfaceVariant,
        )
    }
}
