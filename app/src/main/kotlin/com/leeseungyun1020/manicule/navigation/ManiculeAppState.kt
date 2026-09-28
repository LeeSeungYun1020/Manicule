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
import com.leeseungyun1020.manicule.feature.home.navigation.HomeRoute
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryRoute
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryTab

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
            val current = navController.currentBackStackEntryAsState().value?.destination ?: return null
            return topLevelDestinations.firstOrNull { destination ->
                current.hasRoute(destination.route::class)
            }
        }

    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        if (destination == TopLevelDestination.HOME) {
            if (navController.currentBackStackEntry?.destination?.hasRoute(HomeRoute::class) == true) {
                return
            }
            val popped = navController.popBackStack(HomeRoute, inclusive = false, saveState = true)
            if (!popped) {
                navController.navigate(destination.route, topLevelNavOptions())
            }
            return
        }

        navController.navigate(destination.route, topLevelNavOptions())
    }

    internal fun navigateToLibrary(tab: LibraryTab) {
        navController.navigate(
            route = LibraryRoute(initialTab = tab),
            navOptions = topLevelNavOptions(restoreState = false),
        )
    }

    private fun topLevelNavOptions(restoreState: Boolean = true): NavOptions =
        NavOptions
            .Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(restoreState)
            .setPopUpTo(
                navController.graph.findStartDestination().id,
                inclusive = false,
                saveState = true,
            ).build()
}

@Composable
fun rememberManiculeAppState(
    windowSizeClass: WindowSizeClass,
    navController: NavHostController = rememberNavController(),
): ManiculeAppState =
    remember(navController, windowSizeClass) {
        ManiculeAppState(navController = navController, windowSizeClass = windowSizeClass)
    }
