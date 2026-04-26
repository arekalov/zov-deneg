package com.zovdeneg.app.ui.components

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun ZovAuthPasswordOutlinedField(
    state: ZovAuthPasswordFieldState,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: (@Composable () -> Unit)? = null,
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val c = ZovTheme.colors
    val transformation =
        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
    val cdRes =
        if (passwordVisible) R.string.cd_hide_password else R.string.cd_show_password
    val visibilityIcon =
        if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
    OutlinedTextField(
        value = state.value,
        onValueChange = state.onValueChange,
        label = label,
        supportingText = supportingText,
        visualTransformation = transformation,
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = visibilityIcon,
                    contentDescription = stringResource(cdRes),
                    tint = c.onSurfaceVariant,
                )
            }
        },
        modifier = modifier,
        enabled = state.enabled,
    )
}
