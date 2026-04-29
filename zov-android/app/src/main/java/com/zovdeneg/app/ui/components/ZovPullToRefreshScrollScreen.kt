package com.zovdeneg.app.ui.components

import com.zovdeneg.app.ui.common.ZovHorizontalPadding
import com.zovdeneg.app.ui.common.ZovScrollBodySpacing
import com.zovdeneg.app.ui.common.ZovSpace4
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZovPullToRefreshScrollScreen(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val pullState = rememberPullToRefreshState()
    Box(
        modifier
            .fillMaxSize()
            .background(ZovTheme.colors.background),
        contentAlignment = Alignment.TopCenter,
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = ZovHorizontalPadding)
                    .padding(bottom = ZovSpace4),
                verticalArrangement = Arrangement.spacedBy(ZovScrollBodySpacing),
                content = content,
            )
        }
    }
}
