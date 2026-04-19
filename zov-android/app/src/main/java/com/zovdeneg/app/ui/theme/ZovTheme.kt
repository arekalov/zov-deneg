package com.zovdeneg.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

object ZovTheme {
    val colors: ZovColors
        @Composable
        @ReadOnlyComposable
        get() = LocalZovColors.current

    val text: ZovTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalZovTypography.current
}

private fun ZovColors.toMaterialLight(): ColorScheme = lightColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = primary,
    surface = surface,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    background = background,
)

private fun ZovColors.toMaterialDark(): ColorScheme = darkColorScheme(
    primary = primary,
    onPrimary = onPrimary,
    primaryContainer = primaryContainer,
    onPrimaryContainer = onPrimary,
    surface = surface,
    onSurface = onSurface,
    onSurfaceVariant = onSurfaceVariant,
    outline = outline,
    background = background,
)

@Composable
fun ZovAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val zovColors = if (darkTheme) ZovColors.Dark else ZovColors.Light
    val zovText = ZovTextStyles.Default
    val materialColors = if (darkTheme) zovColors.toMaterialDark() else zovColors.toMaterialLight()
    CompositionLocalProvider(
        LocalZovColors provides zovColors,
        LocalZovTypography provides zovText,
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography(),
            content = content,
        )
    }
}
