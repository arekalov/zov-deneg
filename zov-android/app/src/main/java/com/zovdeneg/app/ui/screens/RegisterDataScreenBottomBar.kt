package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
internal fun RegisterDataScreenBottomBar(onLogin: () -> Unit) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.register_already_have_account),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
        )
        TextButton(
            onClick = onLogin,
            modifier = Modifier.wrapContentWidth(),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            Text(
                stringResource(R.string.action_sign_in),
                style = t.subtitleReg13,
                color = c.primary,
            )
        }
    }
}
