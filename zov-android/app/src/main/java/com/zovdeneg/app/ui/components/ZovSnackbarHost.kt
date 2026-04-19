package com.zovdeneg.app.ui.components

import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovSpace4
import com.zovdeneg.app.ui.common.ZovSpace8
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * [SnackbarHostState] provided at the root [androidx.compose.material3.Scaffold] ([ZovNavHost]).
 * Use [androidx.compose.material3.SnackbarHostState.showSnackbar] for Material 3 snackbars.
 */
val LocalZovSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    error("LocalZovSnackbarHostState: provide via ZovNavHost")
}

private fun Modifier.zovSnackbarHostOuter(applyNavigationBarPadding: Boolean): Modifier =
    this
        .fillMaxWidth()
        .padding(horizontal = ZovHorizontalPadding)
        .then(
            if (applyNavigationBarPadding) {
                Modifier.navigationBarsPadding()
            } else {
                Modifier
            },
        )
        .padding(bottom = ZovSpace4)

@Composable
private fun ZovStyledSnackbar(data: SnackbarData) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val shape = RoundedCornerShape(ZovSpace8)
    val visuals = data.visuals
    val actionLabel = visuals.actionLabel
    Surface(
        modifier = Modifier.border(1.dp, c.primary, shape),
        shape = shape,
        color = c.surface,
        contentColor = c.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = ZovSpace4, vertical = ZovSpace4),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing),
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = c.primary,
            )
            Text(
                text = visuals.message,
                style = t.bodyMed14,
                color = c.onSurface,
                modifier = Modifier.weight(1f),
            )
            if (actionLabel != null) {
                TextButton(onClick = { data.performAction() }) {
                    Text(text = actionLabel, style = t.bodyMed14, color = c.primary)
                }
            }
            if (visuals.withDismissAction) {
                IconButton(onClick = { data.dismiss() }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = c.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Material 3 [SnackbarHost] в стиле макета: светлая плашка, зелёная обводка, скругление «капсула»,
 * слева иконка успеха (галочка). [Surface] вместо [androidx.compose.material3.Snackbar]: у перегрузки
 * с произвольным [content] нет параметров тени/elevation, а внутренняя тень M3 здесь не нужна.
 */
@Composable
fun ZovSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    /**
     * На экранах без нижней навигации слот снекбара у нижнего края экрана; при edge-to-edge нужен
     * inset под системную полоску жестов. Когда виден [NavigationBar], отступ уже заложен в слоте
     * [Scaffold] — повторный inset даёт лишний зазор.
     */
    applyNavigationBarPadding: Boolean = true,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.zovSnackbarHostOuter(applyNavigationBarPadding),
    ) { data ->
        ZovStyledSnackbar(data = data)
    }
}
