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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zovdeneg.app.R
import com.zovdeneg.app.domain.profile.UserProfile
import com.zovdeneg.app.ui.common.ZovHalfUnit
import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovScrollBodySpacing
import com.zovdeneg.app.ui.common.ZovSpace4
import com.zovdeneg.app.ui.common.ZovTightGap
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.LocalZovSnackbarHostState
import com.zovdeneg.app.ui.components.ZovOutlinedRow
import com.zovdeneg.app.ui.components.ZovPinDots
import com.zovdeneg.app.ui.components.ZovPinKeypad
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.components.ZovSummaryCard
import com.zovdeneg.app.ui.profile.ChangePinFlowStep
import com.zovdeneg.app.ui.profile.ChangePinUiState
import com.zovdeneg.app.ui.profile.ChangePinViewModel
import com.zovdeneg.app.ui.profile.EditProfileUiState
import com.zovdeneg.app.ui.profile.EditProfileViewModel
import com.zovdeneg.app.ui.profile.ProfileViewModel
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private data class ChangePinHeaderModel(
    val segment: Int,
    val titleRes: Int,
    val subtitleRes: Int,
)

private data class ProfileLoadedActions(
    val onEditProfile: () -> Unit,
    val onChangePin: () -> Unit,
    val onLogout: () -> Unit,
    val onBiometricUnlockChange: (Boolean) -> Unit,
)

private fun changePinHeaderModel(step: ChangePinFlowStep): ChangePinHeaderModel =
    when (step) {
        ChangePinFlowStep.ENTER_CURRENT ->
            ChangePinHeaderModel(
                segment = 0,
                titleRes = R.string.pin_change_enter_current,
                subtitleRes = R.string.pin_change_step_1_of_3,
            )
        ChangePinFlowStep.ENTER_NEW ->
            ChangePinHeaderModel(
                segment = 1,
                titleRes = R.string.pin_change_create_new,
                subtitleRes = R.string.pin_change_step_2_of_3,
            )
        ChangePinFlowStep.CONFIRM_NEW ->
            ChangePinHeaderModel(
                segment = 2,
                titleRes = R.string.pin_change_repeat_new,
                subtitleRes = R.string.pin_change_step_3_of_3,
            )
    }

@Composable
private fun ProfileScreenLoaded(
    profile: UserProfile,
    biometricUnlockEnabled: Boolean,
    actions: ProfileLoadedActions,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovSummaryCard {
        Text(profile.displayName, style = t.titleSemi20, color = c.onSurface)
        Text(profile.email, style = t.subtitleReg14, color = c.onSurfaceVariant)
        Text(profile.phone, style = t.subtitleReg14, color = c.onSurfaceVariant)
    }
    TextButton(onClick = actions.onEditProfile) {
        Text(
            stringResource(R.string.profile_edit),
            color = c.primary,
            style = t.bodyMed14,
        )
    }
    Text(
        stringResource(R.string.profile_section_security),
        style = t.captionUpper11,
        color = c.onSurfaceVariant,
    )
    ZovOutlinedRow(onClick = actions.onChangePin) {
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
    Text(
        stringResource(R.string.profile_section_app),
        style = t.captionUpper11,
        color = c.onSurfaceVariant,
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.profile_sign_in_fingerprint),
            style = t.bodyReg14,
            color = c.onSurface,
        )
        Switch(
            checked = biometricUnlockEnabled,
            onCheckedChange = actions.onBiometricUnlockChange,
        )
    }
    Button(onClick = actions.onLogout, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_sign_out), style = t.bodyMed14)
    }
}

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
                ProfileScreenLoaded(
                    profile = p,
                    biometricUnlockEnabled = state.biometricUnlockEnabled,
                    actions = ProfileLoadedActions(
                        onEditProfile = onEditProfile,
                        onChangePin = onChangePin,
                        onLogout = onLogout,
                        onBiometricUnlockChange = viewModel::setBiometricUnlockEnabled,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EditProfileFormFields(
    state: EditProfileUiState,
    viewModel: EditProfileViewModel,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
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
        Text(
            stringResource(R.string.error_load),
            style = t.bodyReg14,
            color = c.negative,
        )
    }
    Button(
        onClick = { viewModel.save() },
        enabled = !state.isSaving,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.action_save))
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
    val snackbarHostState = LocalZovSnackbarHostState.current
    val savedMsg = stringResource(R.string.profile_save_success)
    LaunchedEffect(state.saveSucceeded) {
        if (!state.saveSucceeded) return@LaunchedEffect
        snackbarHostState.showSnackbar(savedMsg)
        viewModel.acknowledgeSave()
        onBack()
    }
    ZovScrollScreen {
        when {
            state.isLoading -> CircularProgressIndicator(color = c.primary)
            state.loadFailed -> {
                Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
                Button(onClick = onBack) { Text(stringResource(R.string.action_back)) }
            }

            else -> {
                EditProfileFormFields(state = state, viewModel = viewModel)
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

private data class ChangePinScrollModel(
    val activeSegment: Int,
    val titleRes: Int,
    val subtitleRes: Int,
    val pinLen: Int,
    val state: ChangePinUiState,
    val forgotMsg: String,
)

@Composable
private fun ChangePinInlineErrors(state: ChangePinUiState) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
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
}

@Composable
private fun ChangePinScrollSection(
    modifier: Modifier,
    model: ChangePinScrollModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ZovScrollBodySpacing),
    ) {
        ChangePinProgress(model.activeSegment)
        Text(
            text = stringResource(model.titleRes),
            style = t.titleSemi20,
            color = c.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stringResource(model.subtitleRes),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        ZovPinDots(filledCount = model.pinLen, total = 4)
        ChangePinInlineErrors(model.state)
        if (model.state.step == ChangePinFlowStep.ENTER_CURRENT) {
            TextButton(
                onClick = {
                    scope.launch {
                        snackbarHostState.showSnackbar(model.forgotMsg)
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
}

@Composable
private fun ChangePinKeypadFooter(
    pinLen: Int,
    isSubmitting: Boolean,
    viewModel: ChangePinViewModel,
) {
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
                if (pinLen == 4 && !isSubmitting) {
                    viewModel.onKeypadConfirm()
                }
            },
        )
    }
}

@Composable
fun ChangePinScreen(
    viewModel: ChangePinViewModel,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalZovSnackbarHostState.current
    val scope = rememberCoroutineScope()
    val okMsg = stringResource(R.string.pin_change_success)
    val forgotMsg = stringResource(R.string.pin_change_forgot_snackbar)
    val pinLen = state.draft.length
    val header = changePinHeaderModel(state.step)
    LaunchedEffect(state.succeeded) {
        if (!state.succeeded) return@LaunchedEffect
        snackbarHostState.showSnackbar(okMsg)
        viewModel.acknowledge()
        onBack()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(c.background),
    ) {
        Column(Modifier.fillMaxSize()) {
            ChangePinScrollSection(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ZovHorizontalPadding)
                    .padding(top = ZovSpace4, bottom = ZovItemSpacing),
                model = ChangePinScrollModel(
                    activeSegment = header.segment,
                    titleRes = header.titleRes,
                    subtitleRes = header.subtitleRes,
                    pinLen = pinLen,
                    state = state,
                    forgotMsg = forgotMsg,
                ),
                snackbarHostState = snackbarHostState,
                scope = scope,
            )
            ChangePinKeypadFooter(
                pinLen = pinLen,
                isSubmitting = state.isSubmitting,
                viewModel = viewModel,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreviewLight() {
    ZovAppTheme(darkTheme = false) {
        Text("Profile preview")
    }
}
