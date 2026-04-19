package com.zovdeneg.app.ui.screens

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovAuthBiometricIcon
import com.zovdeneg.app.ui.common.ZovAuthBiometricRing
import com.zovdeneg.app.ui.common.ZovAuthTopInset
import com.zovdeneg.app.ui.common.ZovContentMaxWidth
import com.zovdeneg.app.ui.common.ZovFieldInnerPadding
import com.zovdeneg.app.ui.common.ZovHalfUnit
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.common.ZovSpace4
import com.zovdeneg.app.ui.common.ZovSpace6
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.ZovBiometricFilledButton
import com.zovdeneg.app.ui.components.ZovPinDots
import com.zovdeneg.app.ui.components.ZovPinKeypad
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var pinLen by remember { mutableIntStateOf(0) }
    Box(
        Modifier
            .fillMaxSize()
            .background(c.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .widthIn(max = ZovContentMaxWidth)
                .padding(ZovHorizontalPadding)
                .padding(top = ZovAuthTopInset),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(ZovSpace6),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ZovTightGap),
            ) {
                Text(stringResource(R.string.auth_welcome), style = t.titleSemi22, color = c.onSurface)
                Text(
                    stringResource(R.string.auth_enter_pin),
                    style = t.subtitleReg14,
                    color = c.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            ZovPinDots(filledCount = pinLen, total = 4)
            ZovPinKeypad(
                modifier = Modifier.fillMaxWidth(),
                onDigit = { if (pinLen < 4) pinLen++ },
                onDelete = { if (pinLen > 0) pinLen-- },
                onConfirm = {
                    if (pinLen == 4) onLoggedIn()
                },
            )
            ZovBiometricFilledButton(onClick = onLoggedIn) {
                Icon(
                    Icons.Filled.Fingerprint,
                    contentDescription = stringResource(R.string.cd_fingerprint),
                    modifier = Modifier.padding(end = ZovItemSpacing),
                )
                Text(stringResource(R.string.auth_sign_in_biometrics), style = t.bodyMed14, color = c.primary)
            }
            TextButton(onClick = onRegister) {
                Text(stringResource(R.string.auth_create_account), style = t.bodyMed14, color = c.primary)
            }
        }
    }
}

@Composable
private fun RegisterProgress(activeStep: Int) {
    val c = ZovTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZovTightGap),
    ) {
        repeat(4) { i ->
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
fun RegisterDataScreen(
    onNext: () -> Unit,
    onLogin: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val fields =
        listOf(
            stringResource(R.string.field_first_name) to stringResource(R.string.sample_first_name),
            stringResource(R.string.field_last_name) to stringResource(R.string.sample_last_name),
            stringResource(R.string.field_phone) to stringResource(R.string.sample_phone_masked),
            stringResource(R.string.field_email) to stringResource(R.string.sample_email),
        )
    ZovScrollScreen {
        RegisterProgress(0)
        Spacer(Modifier.height(ZovItemSpacing))
        Text(stringResource(R.string.register_personal_details), style = t.titleSemi20, color = c.onSurface)
        Text(stringResource(R.string.register_step_1_of_4), style = t.subtitleReg13, color = c.onSurfaceVariant)
        fields.forEach { pair ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ZovUnit)) {
                Text(pair.first, style = t.labelMed12, color = c.onSurfaceVariant)
                Text(
                    pair.second,
                    style = t.bodyReg14,
                    color = c.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
                        .padding(ZovFieldInnerPadding),
                )
            }
        }
        Text(
            stringResource(R.string.register_verify_hint),
            style = t.labelReg12,
            color = c.onSurfaceVariant,
        )
        Button(
            onClick = onNext,
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
fun RegisterPinScreen(onNext: () -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var pinLen by remember { mutableIntStateOf(0) }
    ZovScrollScreen {
        RegisterProgress(1)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ZovTightGap),
            ) {
                Text(stringResource(R.string.register_create_pin), style = t.titleSemi20, color = c.onSurface)
                Text(stringResource(R.string.register_step_2_of_4), style = t.subtitleReg13, color = c.onSurfaceVariant)
            }
            Spacer(Modifier.height(ZovSpace4))
            ZovPinDots(filledCount = pinLen, total = 4)
            ZovPinKeypad(
                modifier = Modifier.fillMaxWidth(),
                onDigit = { if (pinLen < 4) pinLen++ },
                onDelete = { if (pinLen > 0) pinLen-- },
                onConfirm = { if (pinLen == 4) onNext() },
            )
            Spacer(Modifier.height(ZovSpace4))
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
            ) { Text(stringResource(R.string.action_continue), style = t.bodyMed14) }
        }
    }
}

@Composable
fun RegisterPinConfirmScreen(onNext: () -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovScrollScreen {
        RegisterProgress(2)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.register_confirm_pin), style = t.titleSemi20, color = c.onSurface)
            Text(stringResource(R.string.register_step_3_of_4), style = t.subtitleReg13, color = c.onSurfaceVariant)
            Spacer(Modifier.height(ZovSpace6))
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
            ) { Text(stringResource(R.string.action_continue), style = t.bodyMed14) }
        }
    }
}

@Composable
fun RegisterBiometricScreen(onDone: () -> Unit) {
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
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = c.primary, contentColor = c.onPrimary),
            ) { Text(stringResource(R.string.action_allow), style = t.bodyMed14) }
            OutlinedButton(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_skip), style = t.bodyMed14, color = c.onSurfaceVariant)
            }
        }
    }
}

@Preview(name = "Светлая", showBackground = true, locale = "ru")
@Composable
private fun LoginPreviewLight() {
    ZovAppTheme(darkTheme = false) { LoginScreen({}, {}) }
}

@Preview(name = "Тёмная", showBackground = true, locale = "ru")
@Composable
private fun LoginPreviewDark() {
    ZovAppTheme(darkTheme = true) { LoginScreen({}, {}) }
}
