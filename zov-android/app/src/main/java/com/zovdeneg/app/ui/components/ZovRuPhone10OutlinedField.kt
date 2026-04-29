package com.zovdeneg.app.ui.components

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.auth.isRegisterPhoneComplete
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType

/**
 * Российский мобильный: префикс +7 и ровно 10 цифр в поле (как на регистрации).
 */
@Composable
fun ZovRuPhone10OutlinedField(
    phoneDigits10: String,
    onPhoneDigits10Change: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    validationError: Boolean = false,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val phoneOk = isRegisterPhoneComplete(phoneDigits10)
    OutlinedTextField(
        value = phoneDigits10,
        onValueChange = onPhoneDigits10Change,
        label = { Text(stringResource(R.string.field_phone)) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        prefix = {
            Text(
                text = "+7 ",
                style = t.bodyReg14,
                color = c.onSurfaceVariant,
            )
        },
        supportingText = {
            if (validationError && !phoneOk) {
                Text(
                    stringResource(R.string.error_register_phone_incomplete),
                    style = t.labelReg12,
                    color = c.negative,
                )
            } else {
                Text(
                    stringResource(R.string.hint_register_phone),
                    style = t.labelReg12,
                    color = c.onSurfaceVariant,
                )
            }
        },
        isError = validationError && !phoneOk,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        singleLine = true,
    )
}
