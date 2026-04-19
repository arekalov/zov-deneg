package com.zovdeneg.app.navigation

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.common.ZovSpace6
import com.zovdeneg.app.ui.common.ZovTopBarContentHeight
import com.zovdeneg.app.ui.common.ZovTouchMin
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.LocalZovSnackbarHostState
import com.zovdeneg.app.ui.components.LocalZovSnackbarScope
import com.zovdeneg.app.ui.components.ZovSnackbarHost
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun bottomTabDestinations(): List<BottomDestination> =
    listOf(
        BottomDestination(
            ZovRoutes.MAIN_HOME,
            stringResource(R.string.nav_bottom_home),
            Icons.Filled.Home,
        ),
        BottomDestination(
            ZovRoutes.MAIN_SEARCH,
            stringResource(R.string.nav_bottom_search),
            Icons.Filled.Search,
        ),
        BottomDestination(
            ZovRoutes.MAIN_HISTORY,
            stringResource(R.string.nav_bottom_history),
            Icons.Filled.History,
        ),
    )

private val registerRoutes =
    setOf(
        ZovRoutes.REGISTER_STEP1,
        ZovRoutes.REGISTER_STEP2,
        ZovRoutes.REGISTER_STEP3,
        ZovRoutes.REGISTER_STEP4,
    )

private val mainTabRoutes =
    setOf(
        ZovRoutes.MAIN_HOME,
        ZovRoutes.MAIN_SEARCH,
        ZovRoutes.MAIN_HISTORY,
    )

private val bottomBarRoutes =
    mainTabRoutes + ZovRoutes.PROFILE

@Composable
internal fun zovTopBarTitle(route: String?, backStack: NavBackStackEntry?): String? {
    val ticker = backStack?.arguments?.getString("ticker").orEmpty().replace('_', '/')
    return when (route) {
        ZovRoutes.MAIN_HOME -> stringResource(R.string.nav_top_home)
        ZovRoutes.MAIN_SEARCH -> stringResource(R.string.nav_top_search)
        ZovRoutes.MAIN_HISTORY -> stringResource(R.string.nav_top_history)
        ZovRoutes.PROFILE -> stringResource(R.string.nav_top_profile)
        ZovRoutes.EDIT_PROFILE -> stringResource(R.string.nav_top_edit_profile)
        ZovRoutes.CHANGE_PIN -> stringResource(R.string.nav_top_change_pin)
        ZovRoutes.DEPOSIT -> stringResource(R.string.nav_top_deposit)
        in registerRoutes -> stringResource(R.string.nav_top_sign_up)
        ZovRoutes.DETAIL -> stringResource(R.string.nav_top_detail_prefix) + ticker
        ZovRoutes.BUY -> stringResource(R.string.nav_top_buy_prefix) + ticker
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZovNavTopBar(
    topTitle: String,
    showBack: Boolean,
    showProfileAction: Boolean,
    navController: NavHostController,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Surface(color = c.background, tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Column(Modifier.statusBarsPadding()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(ZovTopBarContentHeight)
                    .padding(horizontal = ZovUnit),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.width(ZovTouchMin),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showBack) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                                tint = c.onSurface,
                            )
                        }
                    }
                }
                Text(
                    text = topTitle,
                    style = t.titleSemi20,
                    color = c.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    Modifier.width(ZovTouchMin),
                    contentAlignment = Alignment.Center,
                ) {
                    if (showProfileAction) {
                        IconButton(onClick = { navController.navigate(ZovRoutes.PROFILE) }) {
                            Icon(
                                Icons.Filled.Person,
                                contentDescription = stringResource(R.string.cd_nav_profile),
                                tint = c.onSurface,
                                modifier = Modifier.size(ZovSpace6),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZovNavBottomBar(
    visible: Boolean,
    current: NavDestination?,
    navController: NavHostController,
) {
    if (!visible) return
    val c = ZovTheme.colors
    val t = ZovTheme.text
    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = c.background,
        contentColor = c.onSurface,
        tonalElevation = 0.dp,
    ) {
        bottomTabDestinations().forEach { dest ->
            val selected = current?.hierarchy?.any { it.route == dest.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(dest.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        dest.icon,
                        contentDescription = dest.label,
                        modifier = Modifier.size(ZovSpace6),
                    )
                },
                label = {
                    Text(dest.label, style = t.subtitleReg13, maxLines = 1)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = c.primary,
                    selectedTextColor = c.primary,
                    unselectedIconColor = c.onSurfaceVariant,
                    unselectedTextColor = c.onSurfaceVariant,
                    indicatorColor = c.primaryContainer,
                ),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZovNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()
    val backStack by navController.currentBackStackEntryAsState()
    val current = backStack?.destination
    val route = current?.route
    val showBottomBar = route in bottomBarRoutes
    val showProfileAction = route in mainTabRoutes
    val topTitle = zovTopBarTitle(route, backStack)
    val showBack = topTitle != null && route !in mainTabRoutes

    CompositionLocalProvider(
        LocalZovSnackbarHostState provides snackbarHostState,
        LocalZovSnackbarScope provides snackbarScope,
    ) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            containerColor = ZovTheme.colors.background,
            snackbarHost = {
                ZovSnackbarHost(
                    hostState = snackbarHostState,
                    applyNavigationBarPadding = !showBottomBar,
                )
            },
            topBar = {
                if (topTitle != null) {
                    ZovNavTopBar(
                        topTitle = topTitle,
                        showBack = showBack,
                        showProfileAction = showProfileAction,
                        navController = navController,
                    )
                }
            },
            bottomBar = {
                ZovNavBottomBar(
                    visible = showBottomBar,
                    current = current,
                    navController = navController,
                )
            },
        ) { inner ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.TopCenter,
            ) {
                ZovNavGraphHost(navController = navController, modifier = Modifier.fillMaxSize())
            }
        }
    }
}
