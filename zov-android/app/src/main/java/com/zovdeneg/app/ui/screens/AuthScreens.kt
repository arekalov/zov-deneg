package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.auth.RegisterStep1UiState
import com.zovdeneg.app.ui.auth.ZovLoginViewModel
import com.zovdeneg.app.ui.auth.ZovRegisterFlowViewModel
import com.zovdeneg.app.ui.auth.ZovRegisterStep1ViewModel
import com.zovdeneg.app.ui.auth.canAuthenticateWithBiometric
import com.zovdeneg.app.ui.auth.showFingerprintLoginPrompt
import com.zovdeneg.app.ui.common.ZovAuthBiometricIcon
import com.zovdeneg.app.ui.common.ZovAuthBiometricRing
import com.zovdeneg.app.ui.common.ZovAuthTopInset
import com.zovdeneg.app.ui.common.ZovContentMaxWidth
import com.zovdeneg.app.ui.common.ZovHalfUnit
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovSpace4
import com.zovdeneg.app.ui.common.ZovSpace6
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.ZovAuthPasswordFieldState
import com.zovdeneg.app.ui.components.ZovAuthPasswordOutlinedField
import com.zovdeneg.app.ui.components.ZovBiometricFilledButton
import com.zovdeneg.app.ui.components.ZovCenteredCircularProgress
import com.zovdeneg.app.ui.components.ZovPinDots
import com.zovdeneg.app.ui.components.ZovPinKeypad
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zovdeneg.app.BuildConfig

private data class LoginScreenPinState(
    val pinLen: Int,
    val wrongPin: Boolean,
    val mustRegisterFirst: Boolean,
    val remoteSessionFailed: Boolean,
    val remoteSessionSyncing: Boolean,
)

private data class LoginScreenCallbacks(
    val onLoggedIn: () -> Unit,
    val onRegister: () -> Unit,
    val onNeedPinSetupAfterLogin: () -> Unit,
)

@Composable
private fun LoginWelcomeBlock(title: String, subtitle: String) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ZovTightGap),
    ) {
        Text(title, style = t.titleSemi22, color = c.onSurface)
        Text(
            subtitle,
            style = t.subtitleReg14,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LoginPinFeedbackTexts(
    pinState: LoginScreenPinState,
    biometricHint: String?,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    if (pinState.wrongPin) {
        Text(
            stringResource(R.string.login_wrong_pin),
            style = t.bodyReg14,
            color = c.negative,
            textAlign = TextAlign.Center,
        )
    }
    if (pinState.mustRegisterFirst) {
        Text(
            stringResource(R.string.login_register_first),
            style = t.bodyReg14,
            color = c.negative,
            textAlign = TextAlign.Center,
        )
    }
    if (pinState.remoteSessionFailed) {
        Text(
            stringResource(R.string.login_failed_network),
            style = t.bodyReg14,
            color = c.negative,
            textAlign = TextAlign.Center,
        )
    }
    biometricHint?.let { hint ->
        Text(
            hint,
            style = t.bodyReg14,
            color = c.negative,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
internal fun LoginScreen(
    viewModel: ZovLoginViewModel,
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit,
    onNeedPinSetupAfterLogin: () -> Unit,
) {
    val c = ZovTheme.colors
    val pinMode = viewModel.shouldShowPinUnlock()
    val draftPin by viewModel.draftPin.collectAsStateWithLifecycle()
    val wrongPin by viewModel.wrongPin.collectAsStateWithLifecycle()
    val mustRegisterFirst by viewModel.mustRegisterFirst.collectAsStateWithLifecycle()
    val remoteSessionFailed by viewModel.remoteSessionFailed.collectAsStateWithLifecycle()
    val remoteSessionSyncing by viewModel.remoteSessionSyncing.collectAsStateWithLifecycle()
    Box(
        Modifier
            .fillMaxSize()
            .background(c.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        LoginScreenColumn(
            pinMode = pinMode,
            pinState = LoginScreenPinState(
                pinLen = draftPin.length,
                wrongPin = wrongPin,
                mustRegisterFirst = mustRegisterFirst,
                remoteSessionFailed = remoteSessionFailed,
                remoteSessionSyncing = remoteSessionSyncing,
            ),
            viewModel = viewModel,
            callbacks = LoginScreenCallbacks(
                onLoggedIn = onLoggedIn,
                onRegister = onRegister,
                onNeedPinSetupAfterLogin = onNeedPinSetupAfterLogin,
            ),
        )
    }
}

@Composable
private fun LoginBiometricBlock(
    viewModel: ZovLoginViewModel,
    remoteSessionSyncing: Boolean,
    onLoggedIn: () -> Unit,
    onBiometricHint: (String?) -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val context = LocalContext.current
    val biometricLoginDisabledMsg = stringResource(R.string.biometric_login_disabled)
    val biometricHardwareUnavailableMsg = stringResource(R.string.biometric_hardware_unavailable)
    ZovBiometricFilledButton(
        onClick = {
            if (remoteSessionSyncing) return@ZovBiometricFilledButton
            onBiometricHint(null)
            val act = context as? FragmentActivity ?: return@ZovBiometricFilledButton
            if (!viewModel.isBiometricUnlockEnabled()) {
                onBiometricHint(biometricLoginDisabledMsg)
                return@ZovBiometricFilledButton
            }
            if (!canAuthenticateWithBiometric(act)) {
                onBiometricHint(biometricHardwareUnavailableMsg)
                return@ZovBiometricFilledButton
            }
            showFingerprintLoginPrompt(
                activity = act,
                onSuccess = { viewModel.onBiometricAuthenticated(onLoggedIn) },
                onError = { msg -> if (msg.isNotBlank()) onBiometricHint(msg) },
            )
        },
    ) {
        Icon(
            Icons.Filled.Fingerprint,
            contentDescription = stringResource(R.string.cd_fingerprint),
            modifier = Modifier.padding(end = ZovItemSpacing),
        )
        Text(stringResource(R.string.auth_sign_in_biometrics), style = t.bodyMed14, color = c.primary)
    }
}

@Composable
private fun LoginScreenColumn(
    pinMode: Boolean,
    pinState: LoginScreenPinState,
    viewModel: ZovLoginViewModel,
    callbacks: LoginScreenCallbacks,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var biometricHint by remember { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .widthIn(max = ZovContentMaxWidth)
            .padding(ZovHorizontalPadding)
            .padding(top = ZovAuthTopInset),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ZovSpace6),
    ) {
        if (pinMode) {
            LoginWelcomeBlock(
                title = stringResource(R.string.auth_welcome),
                subtitle = stringResource(R.string.auth_enter_pin),
            )
            ZovPinDots(filledCount = pinState.pinLen, total = 4)
            LoginPinFeedbackTexts(pinState = pinState, biometricHint = biometricHint)
            if (pinState.remoteSessionSyncing) {
                ZovCenteredCircularProgress()
            }
            ZovPinKeypad(
                modifier = Modifier.fillMaxWidth(),
                onDigit = viewModel::appendDigit,
                onDelete = viewModel::deleteLast,
                onConfirm = {
                    if (!pinState.remoteSessionSyncing) {
                        viewModel.tryCompleteLogin(callbacks.onLoggedIn)
                    }
                },
            )
            if (!BuildConfig.IS_BIOMETRY_AVAILABLE) {
                LoginBiometricBlock(
                    viewModel = viewModel,
                    remoteSessionSyncing = pinState.remoteSessionSyncing,
                    onLoggedIn = callbacks.onLoggedIn,
                    onBiometricHint = { biometricHint = it },
                )
            }
        } else {
            LoginCredentialBlock(
                viewModel = viewModel,
                onLoggedIn = callbacks.onLoggedIn,
                onNeedPinSetupAfterLogin = callbacks.onNeedPinSetupAfterLogin,
            )
        }
        TextButton(onClick = callbacks.onRegister) {
            Text(stringResource(R.string.auth_create_account), style = t.bodyMed14, color = c.primary)
        }
    }
}

@Composable
private fun LoginCredentialBlock(
    viewModel: ZovLoginViewModel,
    onLoggedIn: () -> Unit,
    onNeedPinSetupAfterLogin: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val phone by viewModel.credentialPhone.collectAsStateWithLifecycle()
    val password by viewModel.credentialPassword.collectAsStateWithLifecycle()
    val credentialError by viewModel.credentialError.collectAsStateWithLifecycle()
    val submitting by viewModel.credentialSubmitting.collectAsStateWithLifecycle()
    LoginWelcomeBlock(
        title = stringResource(R.string.auth_welcome),
        subtitle = stringResource(R.string.auth_login_phone_password_subtitle),
    )
    OutlinedTextField(
        value = phone,
        onValueChange = viewModel::setCredentialPhone,
        label = { Text(stringResource(R.string.field_phone)) },
        modifier = Modifier.fillMaxWidth(),
        enabled = !submitting,
    )
    ZovAuthPasswordOutlinedField(
        state = ZovAuthPasswordFieldState(
            value = password,
            onValueChange = viewModel::setCredentialPassword,
            enabled = !submitting,
        ),
        label = { Text(stringResource(R.string.field_password)) },
        modifier = Modifier.fillMaxWidth(),
    )
    credentialError?.let { err ->
        Text(
            err,
            style = t.bodyReg14,
            color = c.negative,
            textAlign = TextAlign.Center,
        )
    }
    if (submitting) {
        ZovCenteredCircularProgress()
    }
    Button(
        onClick = {
            viewModel.submitCredentials(
                onLoggedIn = onLoggedIn,
                onNeedPinSetup = onNeedPinSetupAfterLogin,
            )
        },
        enabled = !submitting && phone.isNotBlank() && password.isNotBlank(),
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
    ) {
        Text(stringResource(R.string.action_sign_in), style = t.bodyMed14)
    }
}

@Composable
private fun RegisterProgress(activeStep: Int) {
    val c = ZovTheme.colors

    val stepsCount = if (BuildConfig.IS_BIOMETRY_AVAILABLE) 3 else 4

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZovTightGap),
    ) {
        repeat(stepsCount) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(ZovUnit)
                    .clip(RoundedCornerShape(ZovHalfUnit))
                    .background(
                        if (i <= activeStep) {
                            c.primary.copy(alpha = if (i < activeStep) 0.45f else 1f)
                        } else {
                            c.outline
                        },
                    ),
            )
        }
    }
}

@Composable
private fun RegisterStep1TextFields(
    state: RegisterStep1UiState,
    viewModel: ZovRegisterStep1ViewModel,
) {
    OutlinedTextField(
        value = state.firstName,
        onValueChange = viewModel::setFirstName,
        label = { Text(stringResource(R.string.field_first_name)) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.lastName,
        onValueChange = viewModel::setLastName,
        label = { Text(stringResource(R.string.field_last_name)) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.phone,
        onValueChange = viewModel::setPhone,
        label = { Text(stringResource(R.string.field_phone)) },
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = state.email,
        onValueChange = viewModel::setEmail,
        label = { Text(stringResource(R.string.field_email)) },
        modifier = Modifier.fillMaxWidth(),
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

@Composable
fun RegisterDataScreen(
    viewModel: ZovRegisterStep1ViewModel,
    onNext: () -> Unit,
    onLogin: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ZovScrollScreen {
        RegisterProgress(0)
        Spacer(Modifier.height(ZovItemSpacing))
        Text(stringResource(R.string.register_personal_details), style = t.titleSemi20, color = c.onSurface)
        Text(stringResource(
            if (BuildConfig.IS_BIOMETRY_AVAILABLE)
                R.string.register_step_1_of_3
            else
                R.string.register_step_1_of_4
        ), style = t.subtitleReg13, color = c.onSurfaceVariant)
        RegisterStep1TextFields(state = state, viewModel = viewModel)
        if (state.validationError) {
            Text(
                stringResource(R.string.register_validation_error),
                style = t.bodyReg14,
                color = c.negative,
            )
        }
        state.submitError?.let { err ->
            Text(
                err,
                style = t.bodyReg14,
                color = c.negative,
            )
        }
        Text(
            stringResource(R.string.register_verify_hint),
            style = t.labelReg12,
            color = c.onSurfaceVariant,
        )
        Button(
            onClick = { viewModel.submit(onNext) },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
        ) { Text(stringResource(R.string.action_continue), style = t.bodyMed14) }
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                stringResource(R.string.register_already_have_account),
                style = t.subtitleReg13,
                color = c.onSurfaceVariant,
            )
            TextButton(onClick = onLogin) {
                Text(
                    stringResource(R.string.action_sign_in),
                    style = t.subtitleReg13,
                    color = c.primary,
                )
            }
        }
    }
}

@Composable
internal fun RegisterPinScreen(
    viewModel: ZovRegisterFlowViewModel,
    onContinue: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val draftPin by viewModel.draftPin.collectAsStateWithLifecycle()
    val pinLen = draftPin.length
    ZovScrollScreen {
        RegisterProgress(1)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ZovTightGap),
            ) {
                Text(stringResource(R.string.register_create_pin), style = t.titleSemi20, color = c.onSurface)
                Text(stringResource(
                    if (BuildConfig.IS_BIOMETRY_AVAILABLE)
                        R.string.register_step_2_of_3
                    else
                        R.string.register_step_2_of_4
                ), style = t.subtitleReg13, color = c.onSurfaceVariant)
            }
            Spacer(Modifier.height(ZovSpace4))
            ZovPinDots(filledCount = pinLen, total = 4)
            ZovPinKeypad(
                modifier = Modifier.fillMaxWidth(),
                onDigit = viewModel::appendDigit,
                onDelete = viewModel::deleteLast,
                onConfirm = { if (pinLen == 4) onContinue() },
            )
            Spacer(Modifier.height(ZovSpace4))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
            ) { Text(stringResource(R.string.action_continue), style = t.bodyMed14) }
        }
    }
}

@Composable
internal fun RegisterPinConfirmScreen(
    viewModel: ZovRegisterFlowViewModel,
    onContinue: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val draftPin by viewModel.draftPin.collectAsStateWithLifecycle()
    val pinMismatch by viewModel.confirmPinMismatch.collectAsStateWithLifecycle()
    val pinLen = draftPin.length
    ZovScrollScreen {
        RegisterProgress(2)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ZovTightGap),
            ) {
                Text(stringResource(R.string.register_confirm_pin), style = t.titleSemi20, color = c.onSurface)
                Text(stringResource(
                    if (BuildConfig.IS_BIOMETRY_AVAILABLE)
                        R.string.register_step_3_of_3
                    else
                        R.string.register_step_3_of_4
                ), style = t.subtitleReg13, color = c.onSurfaceVariant)
            }
            Spacer(Modifier.height(ZovSpace4))
            ZovPinDots(filledCount = pinLen, total = 4)
            if (pinMismatch) {
                Spacer(Modifier.height(ZovUnit))
                Text(
                    stringResource(R.string.register_pin_mismatch),
                    modifier = Modifier.fillMaxWidth(),
                    style = t.bodyReg14,
                    color = c.negative,
                    textAlign = TextAlign.Center,
                )
            }
            ZovPinKeypad(
                modifier = Modifier.fillMaxWidth(),
                onDigit = viewModel::appendDigit,
                onDelete = viewModel::deleteLast,
                onConfirm = { if (pinLen == 4) onContinue() },
            )
            Spacer(Modifier.height(ZovSpace4))
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
            ) { Text(stringResource(R.string.action_continue), style = t.bodyMed14) }
        }
    }
}

@Composable
fun RegisterBiometricScreen(
    onAllow: () -> Unit,
    onSkip: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovScrollScreen {
        RegisterProgress(3)
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ZovSpace4),
        ) {
            Box(
                Modifier
                    .size(ZovAuthBiometricRing)
                    .clip(CircleShape)
                    .background(c.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.Fingerprint,
                    contentDescription = stringResource(R.string.cd_fingerprint),
                    tint = c.primary,
                    modifier = Modifier.size(ZovAuthBiometricIcon),
                )
            }
            Text(
                stringResource(R.string.register_biometric_title),
                style = t.titleSemi20,
                color = c.onSurface,
                textAlign = TextAlign.Center,
            )
            Text(
                stringResource(R.string.register_biometric_body),
                style = t.subtitleReg14,
                color = c.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Text(stringResource(R.string.register_step_4_of_4), style = t.subtitleReg13, color = c.onSurfaceVariant)
            Button(
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
            ) { Text(stringResource(R.string.action_allow), style = t.bodyMed14) }
            OutlinedButton(onClick = onSkip, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_skip), style = t.bodyMed14, color = c.onSurfaceVariant)
            }
        }
    }
}

@Preview(name = "Светлая", showBackground = true, locale = "ru")
@Composable
private fun LoginPreviewLight() {
    ZovAppTheme(darkTheme = false) {
        Text(stringResource(R.string.auth_welcome))
    }
}

@Preview(name = "Тёмная", showBackground = true, locale = "ru")
@Composable
private fun LoginPreviewDark() {
    ZovAppTheme(darkTheme = true) {
        Text(stringResource(R.string.auth_welcome))
    }
}
