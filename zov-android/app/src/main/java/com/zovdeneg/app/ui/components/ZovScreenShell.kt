package com.zovdeneg.app.ui.components

import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovScrollBodySpacing
import com.zovdeneg.app.ui.common.ZovSpace4
import com.zovdeneg.app.ui.theme.ZovAppTheme
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ZovCenteredCircularProgress(modifier: Modifier = Modifier) {
    Box(
        modifier.fillMaxWidth().padding(vertical = ZovSpace4),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ZovTheme.colors.primary)
    }
}

@Composable
fun ZovScrollScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scroll = rememberScrollState()
    Box(
        modifier
            .fillMaxSize()
            .background(ZovTheme.colors.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(horizontal = ZovHorizontalPadding)
                .padding(bottom = ZovSpace4),
            verticalArrangement = Arrangement.spacedBy(ZovScrollBodySpacing),
            content = content,
        )
    }
}

@Preview(name = "ZovScrollScreen · светлая", showBackground = true, locale = "ru", heightDp = 420)
@Composable
private fun ZovScrollScreenPreviewLight() {
    ZovAppTheme(darkTheme = false) {
        Box(Modifier.height(420.dp)) {
            ZovScrollScreen {
                repeat(6) { i ->
                    Text(
                        text = "Блок ${i + 1} · вертикальный шаг ZovScrollBodySpacing",
                        style = ZovTheme.text.bodyReg14,
                        color = ZovTheme.colors.onSurface,
                    )
                }
            }
        }
    }
}

@Preview(name = "ZovScrollScreen · тёмная", showBackground = true, locale = "ru", heightDp = 420)
@Composable
private fun ZovScrollScreenPreviewDark() {
    ZovAppTheme(darkTheme = true) {
        Box(Modifier.height(420.dp)) {
            ZovScrollScreen {
                Text(
                    text = "Пример скролла",
                    style = ZovTheme.text.sectionSemi16,
                    color = ZovTheme.colors.onSurface,
                )
            }
        }
    }
}
