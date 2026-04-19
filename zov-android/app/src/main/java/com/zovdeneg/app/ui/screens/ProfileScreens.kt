package com.zovdeneg.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovFieldInnerPadding
import com.zovdeneg.app.ui.common.ZovHalfUnit
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovScrollBodySpacing
import com.zovdeneg.app.ui.common.ZovSpace3
import com.zovdeneg.app.ui.common.ZovSpace4
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.ZovOutlinedRow
import com.zovdeneg.app.ui.components.ZovPinDots
import com.zovdeneg.app.ui.components.ZovPinKeypad
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.components.ZovSummaryCard
import com.zovdeneg.app.ui.profile.ChangePinFlowStep
import com.zovdeneg.app.ui.profile.ChangePinViewModel
import com.zovdeneg.app.ui.profile.EditProfileViewModel
import com.zovdeneg.app.ui.profile.ProfileViewModel
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEditProfile: () -> Unit,
    onChangePin: () -> Unit,
    onLogout: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ZovScrollScreen {
        when {
            state.isLoading -> CircularProgressIndicator(color = c.primary)
            state.loadFailed -> {
                Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
                Button(onClick = { viewModel.refresh() }) { Text(stringResource(R.string.action_retry)) }
            }
            state.profile != null -> {
                val p = requireNotNull(state.profile)
                ZovSummaryCard {
                    Text(p.displayName, style = t.titleSemi20, color = c.onSurface)
                    Text(p.email, style = t.subtitleReg14, color = c.onSurfaceVariant)
                    Text(p.phone, style = t.subtitleReg14, color = c.onSurfaceVariant)
                }
                TextButton(onClick = onEditProfile) {
                    Text(stringResource(R.string.profile_edit), color = c.primary, style = t.bodyMed14)
                }
                Text(stringResource(R.string.profile_section_security), style = t.captionUpper11, color = c.onSurfaceVariant)
                ZovOutlinedRow(onClick = onChangePin) {
                    Text(
                        stringResource(R.string.profile_change_pin),
                        style = t.bodyReg14,
                        color = c.onSurface,
                        modifier = Modifier.weight(1f),
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.cd_chevron),
                        tint = c.onSurfaceVariant,
                    )
                }
                Text(stringResource(R.string.profile_section_app), style = t.captionUpper11, color = c.onSurfaceVariant)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(stringResource(R.string.profile_sign_in_fingerprint), style = t.bodyReg14, color = c.onSurface)
                    Switch(
                        checked = state.biometricUnlockEnabled,
                        onCheckedChange = viewModel::setBiometricUnlockEnabled,
                    )
                }
                Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.action_sign_out), style = t.bodyMed14)
                }
            }
        }
    }
}

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMsg = stringResource(R.string.profile_save_success)
    LaunchedEffect(state.saveSucceeded) {
        if (!state.saveSucceeded) return@LaunchedEffect
        snackbarHostState.showSnackbar(savedMsg)
        viewModel.acknowledgeSave()
        onBack()
    }
    ZovScrollScreen {
        SnackbarHost(hostState = snackbarHostState)
        when {
            state.isLoading -> CircularProgressIndicator(color = c.primary)
            state.loadFailed -> {
                Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
                Button(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            }
            else -> {
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
                if (state.saveFailed) {
                    Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
                }
                Button(
                    onClick = { viewModel.save() },
                    enabled = !state.isSaving,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }
    }
}

@Composable
private fun ChangePinProgress(activeStep: Int) {
    val c = ZovTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ZovTightGap),
    ) {
        repeat(3) { i ->
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
fun ChangePinScreen(
    viewModel: ChangePinViewModel,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val okMsg = stringResource(R.string.pin_change_success)
    val forgotMsg = stringResource(R.string.pin_change_forgot_snackbar)
    val pinLen = state.draft.length
    val activeSegment =
        when (state.step) {
            ChangePinFlowStep.EnterCurrent -> 0
            ChangePinFlowStep.EnterNew -> 1
            ChangePinFlowStep.ConfirmNew -> 2
        }
    val (titleRes, subtitleRes) =
        when (state.step) {
            ChangePinFlowStep.EnterCurrent ->
                R.string.pin_change_enter_current to R.string.pin_change_step_1_of_3
            ChangePinFlowStep.EnterNew ->
                R.string.pin_change_create_new to R.string.pin_change_step_2_of_3
            ChangePinFlowStep.ConfirmNew ->
                R.string.pin_change_repeat_new to R.string.pin_change_step_3_of_3
        }
    LaunchedEffect(state.succeeded) {
        if (!state.succeeded) return@LaunchedEffect
        snackbarHostState.showSnackbar(okMsg)
        viewModel.acknowledge()
        onBack()
    }
    Box(Modifier.fillMaxSize().background(c.background)) {
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ZovHorizontalPadding)
                    .padding(top = ZovSpace4, bottom = ZovItemSpacing),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ZovScrollBodySpacing),
            ) {
                ChangePinProgress(activeSegment)
                Text(
                    text = stringResource(titleRes),
                    style = t.titleSemi20,
                    color = c.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = stringResource(subtitleRes),
                    style = t.subtitleReg13,
                    color = c.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                ZovPinDots(filledCount = pinLen, total = 4)
                if (state.wrongCurrentPin) {
                    Text(
                        stringResource(R.string.pin_change_wrong_current),
                        modifier = Modifier.fillMaxWidth(),
                        style = t.bodyReg14,
                        color = c.negative,
                        textAlign = TextAlign.Center,
                    )
                }
                if (state.confirmMismatch) {
                    Text(
                        stringResource(R.string.register_pin_mismatch),
                        modifier = Modifier.fillMaxWidth(),
                        style = t.bodyReg14,
                        color = c.negative,
                        textAlign = TextAlign.Center,
                    )
                }
                if (state.failed) {
                    Text(
                        stringResource(R.string.error_load),
                        modifier = Modifier.fillMaxWidth(),
                        style = t.bodyReg14,
                        color = c.negative,
                        textAlign = TextAlign.Center,
                    )
                }
                if (state.step == ChangePinFlowStep.EnterCurrent) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar(forgotMsg)
                            }
                        },
                    ) {
                        Text(
                            stringResource(R.string.pin_change_forgot_current),
                            style = t.bodyMed14,
                            color = c.primary,
                        )
                    }
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ZovHorizontalPadding)
                    .padding(bottom = ZovSpace4),
            ) {
                ZovPinKeypad(
                    modifier = Modifier.fillMaxWidth(),
                    onDigit = viewModel::appendDigit,
                    onDelete = viewModel::deleteLast,
                    onConfirm = {
                        if (pinLen == 4 && !state.isSubmitting) {
                            viewModel.onKeypadConfirm()
                        }
                    },
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = ZovHorizontalPadding)
                    .padding(bottom = ZovSpace4),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreviewLight() {
    ZovAppTheme(darkTheme = false) {
        Text("Profile preview")
    }
}
