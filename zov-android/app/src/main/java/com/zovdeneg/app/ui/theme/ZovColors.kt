package com.zovdeneg.app.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ZovColors(
    val background: Color,
    val surface: Color,
    val surfaceContainer: Color,
    val primary: Color,
    val onPrimary: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val primaryContainer: Color,
    val positive: Color,
    val negative: Color,
) {
    companion object {
        val Light = ZovColors(
            background = Color(0xFFF5F7FA),
            surface = Color(0xFFFFFFFF),
            surfaceContainer = Color(0xFFEDEEF0),
            primary = Color(0xFF219638),
            onPrimary = Color(0xFFFFFFFF),
            onSurface = Color(0xFF17191C),
            onSurfaceVariant = Color(0xFF73757A),
            outline = Color(0xFFE0E3E6),
            primaryContainer = Color(0xFFD9F0DB),
            positive = Color(0xFF0D8C47),
            negative = Color(0xFFB82924),
        )

        val Dark = ZovColors(
            background = Color(0xFF101214),
            surface = Color(0xFF1A1C1E),
            surfaceContainer = Color(0xFF25282B),
            primary = Color(0xFF7FD68E),
            onPrimary = Color(0xFF003910),
            onSurface = Color(0xFFE8EAED),
            onSurfaceVariant = Color(0xFFB0B4BA),
            outline = Color(0xFF3D4249),
            primaryContainer = Color(0xFF1E3D26),
            positive = Color(0xFF5FD68A),
            negative = Color(0xFFFF8A80),
        )
    }
}

val LocalZovColors = staticCompositionLocalOf<ZovColors> {
    error("LocalZovColors: wrap the composition tree in ZovAppTheme { ... }")
}
