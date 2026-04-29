package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.auth.RegisterStep1UiState
import com.zovdeneg.app.ui.auth.isRegisterEmailValid
import com.zovdeneg.app.ui.auth.ZovRegisterStep1ViewModel
import com.zovdeneg.app.ui.components.ZovAuthPasswordFieldState
import com.zovdeneg.app.ui.components.ZovAuthPasswordOutlinedField
import com.zovdeneg.app.ui.components.ZovRuPhone10OutlinedField
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType

@Composable
private fun RegisterStep1EmailField(
    email: String,
    validationError: Boolean,
    onEmailChange: (String) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val emailOk = isRegisterEmailValid(email)
    OutlinedTextField(
        value = email,
        onValueChange = onEmailChange,
        label = { Text(stringResource(R.string.field_email)) },
        modifier = Modifier.fillMaxWidth(),
        supportingText = {
            if (validationError && !emailOk) {
                Text(
                    stringResource(R.string.error_register_email_invalid),
                    style = t.labelReg12,
                    color = c.negative,
                )
            } else {
                Text(
                    stringResource(R.string.hint_register_email),
                    style = t.labelReg12,
                    color = c.onSurfaceVariant,
                )
            }
        },
        isError = validationError && !emailOk,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
    )
}

@Composable
internal fun RegisterStep1TextFields(
    state: RegisterStep1UiState,
    viewModel: ZovRegisterStep1ViewModel,
) {
    OutlinedTextField(
        value = state.firstName,
        onValueChange = viewModel::setFirstName,
        label = { Text(stringResource(R.string.field_first_name)) },
        modifier = Modifier.fillMaxWidth(),
        isError = state.validationError && state.firstName.isBlank(),
        singleLine = true,
    )
    OutlinedTextField(
        value = state.lastName,
        onValueChange = viewModel::setLastName,
        label = { Text(stringResource(R.string.field_last_name)) },
        modifier = Modifier.fillMaxWidth(),
        isError = state.validationError && state.lastName.isBlank(),
        singleLine = true,
    )
    ZovRuPhone10OutlinedField(
        phoneDigits10 = state.phoneDigits10,
        onPhoneDigits10Change = viewModel::setPhoneDigits10,
        validationError = state.validationError,
    )
    RegisterStep1EmailField(
        email = state.email,
        validationError = state.validationError,
        onEmailChange = viewModel::setEmail,
    )
    ZovAuthPasswordOutlinedField(
        state = ZovAuthPasswordFieldState(
            value = state.password,
            onValueChange = viewModel::setPassword,
        ),
        label = { Text(stringResource(R.string.field_password)) },
        modifier = Modifier.fillMaxWidth(),
        supportingText = { Text(stringResource(R.string.hint_password_min)) },
    )
}
