package com.zovdeneg.app.ui.components

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType

@Composable
internal fun ZovRubWholeAmountDigitsField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    allowKopecks: Boolean = false,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val placeholder =
        stringResource(
            if (allowKopecks) {
                R.string.amount_decimal_rub_placeholder
            } else {
                R.string.amount_digits_placeholder
            },
        )
    val amountKeyboardType = if (allowKopecks) KeyboardType.Decimal else KeyboardType.Number
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ZovShapeMedium),
        label = { Text(label, style = t.labelMed12) },
        placeholder = { Text(placeholder, style = t.bodyReg14, color = c.onSurfaceVariant) },
        singleLine = true,
        textStyle = t.pinAmount20.copy(color = c.onSurface),
        keyboardOptions = KeyboardOptions(keyboardType = amountKeyboardType),
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
    )
}
