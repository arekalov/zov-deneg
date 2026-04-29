package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.ui.common.ZovFieldInnerPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue

@Composable
private fun MarketLotsAmountTextField(
    cap: Int,
    lots: Int,
    isSubmitting: Boolean,
    onSetLots: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var tfv by remember {
        mutableStateOf(
            TextFieldValue(
                text = lots.toString(),
                selection = TextRange(lots.toString().length),
            ),
        )
    }
    LaunchedEffect(lots) {
        val digits = tfv.text.filter { it.isDigit() }
        if (digits.isEmpty()) return@LaunchedEffect
        val parsed = digits.toIntOrNull() ?: return@LaunchedEffect
        if (parsed != lots) {
            val s = lots.toString()
            tfv = TextFieldValue(text = s, selection = TextRange(s.length))
        }
    }
    val fieldModifier =
        modifier
            .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
            .padding(ZovFieldInnerPadding)
    BasicTextField(
        value = tfv,
        onValueChange = { new ->
            val d = new.text.filter { it.isDigit() }.take(3)
            if (d.isEmpty()) {
                tfv = TextFieldValue(text = "", selection = new.selection)
                return@BasicTextField
            }
            val coerced = d.toInt().coerceIn(1, cap)
            val s = coerced.toString()
            tfv = TextFieldValue(text = s, selection = TextRange(s.length))
            onSetLots(coerced)
        },
        modifier = fieldModifier,
        enabled = !isSubmitting,
        textStyle = t.pinAmount20.copy(color = c.onSurface),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )
}

@Composable
fun MarketOrderLotsRow(
    lots: Int,
    isSubmitting: Boolean,
    maxLots: Int? = null,
    onBump: (Int) -> Unit,
    onSetLots: (Int) -> Unit,
) {
    val t = ZovTheme.text
    val cap = (maxLots ?: 99).coerceAtLeast(1)
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
    ) {
        OutlinedButton(
            onClick = { onBump(-1) },
            enabled = !isSubmitting && lots > 1,
        ) {
            Text("−", style = t.titleSemi20)
        }
        MarketLotsAmountTextField(
            cap = cap,
            lots = lots,
            isSubmitting = isSubmitting,
            onSetLots = onSetLots,
            modifier = Modifier.weight(1f),
        )
        OutlinedButton(
            onClick = { onBump(1) },
            enabled = !isSubmitting && (maxLots == null || lots < maxLots),
        ) {
            Text("+", style = t.titleSemi20)
        }
    }
}
