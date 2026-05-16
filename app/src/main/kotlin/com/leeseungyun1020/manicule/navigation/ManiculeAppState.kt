package com.leeseungyun1020.manicule.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

/**
 * Manicule 의 최상위 navigation 상태 컨테이너.
 *
 * [TopLevelDestination] 4개 탭 사이의 이동을 책임지며,
 * 현재 destination 이 어느 탭에 속해 있는지를 계산해서 BottomBar 의 selected 상태를 만든다.
 */
@Stable
class ManiculeAppState(
    val navController: NavHostController,
    val windowSizeClass: WindowSizeClass,
) {
    val topLevelDestinations: List<TopLevelDestination> = TopLevelDestination.entries

    /**
     * 현재 백스택의 최상위 destination — BottomBar 활성 표시에 사용.
     */
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val current = navController.currentBackStackEntryAsState().value?.destination
            return when {
                current?.hasRoute<HomeRoute>() == true -> TopLevelDestination.HOME
                current?.hasRoute<LibraryRoute>() == true -> TopLevelDestination.LIBRARY
                current?.hasRoute<StatsRoute>() == true -> TopLevelDestination.STATS
                current?.hasRoute<SettingsRoute>() == true -> TopLevelDestination.SETTINGS
                else -> null
            }
        }

    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        val options =
            NavOptions
                .Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(
                    navController.graph.findStartDestination().id,
                    inclusive = false,
                    saveState = true,
                ).build()
        navController.navigate(destination.route, options)
    }
}

@Composable
fun rememberManiculeAppState(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
): ManiculeAppState =
    remember(navController, windowSizeClass) {
        ManiculeAppState(navController = navController, windowSizeClass = windowSizeClass)
    }
