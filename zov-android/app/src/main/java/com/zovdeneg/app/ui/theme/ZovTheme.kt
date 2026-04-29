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
import androidx.compose.ui.graphics.Color
import com.zovdeneg.app.BuildConfig

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
    val baseColors = if (darkTheme) ZovColors.Dark else ZovColors.Light

    val zovColors =
        if (BuildConfig.IS_BIOMETRY_AVAILABLE) {
            if (darkTheme) {
                baseColors.copy(
                    primary = Color(0xFF4ADE80),
                    primaryContainer = Color(0xFF3A6248),
                    onPrimary = Color(0xFF052E16),
                    positive = Color(0xFF86EFAC),
                )
            } else {
                baseColors.copy(
                    primary = Color(0xFF16A34A),
                    primaryContainer = Color(0xFFC8EDD0),
                    onPrimary = Color(0xFFFFFFFF),
                    positive = Color(0xFF059669),
                )
            }
        } else {
            baseColors
        }

    val zovText = ZovTextStyles.Default
    val materialColors =
        if (darkTheme) zovColors.toMaterialDark() else zovColors.toMaterialLight()

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
