package com.zovdeneg.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovAuthBiometricIcon
import com.zovdeneg.app.ui.common.ZovFieldInnerPadding
import com.zovdeneg.app.ui.common.ZovShapeMedium
import com.zovdeneg.app.ui.common.ZovSpace3
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.ZovOutlinedRow
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.components.ZovSummaryCard
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onChangePin: () -> Unit,
    onLogout: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    var bio by remember { mutableStateOf(true) }
    ZovScrollScreen {
        ZovSummaryCard {
            Text(stringResource(R.string.sample_display_name), style = t.titleSemi20, color = c.onSurface)
            Text(stringResource(R.string.sample_email), style = t.subtitleReg14, color = c.onSurfaceVariant)
            Text(stringResource(R.string.sample_phone_full), style = t.subtitleReg14, color = c.onSurfaceVariant)
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

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val fields =
        listOf(
            stringResource(R.string.field_first_name) to stringResource(R.string.sample_first_name),
            stringResource(R.string.field_last_name) to stringResource(R.string.sample_last_name),
            stringResource(R.string.field_phone) to stringResource(R.string.sample_phone_full),
            stringResource(R.string.field_email) to stringResource(R.string.sample_email),
        )
    ZovScrollScreen {
        fields.forEach { pair ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ZovUnit)) {
                Text(pair.first, style = t.labelMed12, color = c.onSurfaceVariant)
                Text(
                    pair.second,
                    style = t.bodyReg14,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.surfaceContainer, RoundedCornerShape(ZovShapeMedium))
                        .padding(ZovFieldInnerPadding),
                )
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_save))
        }
    }
}

@Composable
fun ChangePinScreen(onBack: () -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val dot = stringResource(R.string.pin_dot)
    ZovScrollScreen {
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
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.action_save_pin))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreviewLight() {
    ZovAppTheme(darkTheme = false) { ProfileScreen({}, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun ProfilePreviewDark() {
    ZovAppTheme(darkTheme = true) { ProfileScreen({}, {}, {}) }
}
