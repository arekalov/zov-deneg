package com.zovdeneg.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovAuthBiometricIcon
import com.zovdeneg.app.ui.common.ZovFieldInnerPadding
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.common.ZovSpace3
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.ZovOutlinedRow
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.components.ZovSummaryCard
import com.zovdeneg.app.ui.profile.ChangePinViewModel
import com.zovdeneg.app.ui.profile.EditProfileViewModel
import com.zovdeneg.app.ui.profile.ProfileViewModel
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onEditProfile: () -> Unit,
    onChangePin: () -> Unit,
    onLogout: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var bio by remember { mutableStateOf(true) }
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
                    Switch(checked = bio, onCheckedChange = { bio = it })
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
fun ChangePinScreen(
    viewModel: ChangePinViewModel,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val dot = stringResource(R.string.pin_dot)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val okMsg = stringResource(R.string.pin_change_success)
    LaunchedEffect(state.succeeded) {
        if (!state.succeeded) return@LaunchedEffect
        snackbarHostState.showSnackbar(okMsg)
        viewModel.acknowledge()
        onBack()
    }
    ZovScrollScreen {
        SnackbarHost(hostState = snackbarHostState)
        Text(stringResource(R.string.pin_current_label), style = t.labelMed12, color = c.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(ZovSpace3)) {
            repeat(4) {
                Box(
                    Modifier
                        .size(ZovAuthBiometricIcon)
                        .background(c.surfaceContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(dot, style = t.titleSemi20, color = c.onSurface)
                }
            }
        }
        Text(stringResource(R.string.pin_new_label), style = t.labelMed12, color = c.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(ZovSpace3)) {
            repeat(4) {
                Box(
                    Modifier
                        .size(ZovAuthBiometricIcon)
                        .background(c.surfaceContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(dot, style = t.titleSemi20, color = c.onSurface)
                }
            }
        }
        if (state.failed) {
            Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
        }
        Button(
            onClick = { viewModel.submit() },
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.action_save_pin))
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
