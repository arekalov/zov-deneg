package com.zovdeneg.app.ui.theme

import android.content.res.Configuration
import android.os.Build

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb

/**
 * Edge-to-edge system bars tinted with [ZovColors.background] for the current configuration.
 * Disables Q+ enforced scrims so the bar color matches the app background.
 */
fun ComponentActivity.applyZovSystemBars() {
    val dark =
        (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
    val scrim =
        if (dark) {
            ZovColors.Dark.background.toArgb()
        } else {
            ZovColors.Light.background.toArgb()
        }
    if (dark) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(scrim),
            navigationBarStyle = SystemBarStyle.dark(scrim),
        )
    } else {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(scrim, scrim),
            navigationBarStyle = SystemBarStyle.light(scrim, scrim),
        )
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        window.isNavigationBarContrastEnforced = false
        window.isStatusBarContrastEnforced = false
    }
}
