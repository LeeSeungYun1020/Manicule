package com.leeseungyun1020.manicule.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.leeseungyun1020.manicule.feature.bookdetail.navigation.BookDetailRoute
import com.leeseungyun1020.manicule.feature.bookdetail.navigation.bookDetailScreen
import com.leeseungyun1020.manicule.feature.home.navigation.HomeRoute
import com.leeseungyun1020.manicule.feature.home.navigation.homeScreen
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryRoute
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryTab
import com.leeseungyun1020.manicule.feature.library.navigation.libraryScreen
import com.leeseungyun1020.manicule.feature.scanner.navigation.ScannerRoute
import com.leeseungyun1020.manicule.feature.scanner.navigation.scannerScreen
import com.leeseungyun1020.manicule.feature.search.SearchScannerAction
import com.leeseungyun1020.manicule.feature.search.navigation.SearchRoute
import com.leeseungyun1020.manicule.feature.search.navigation.searchScreen
import com.leeseungyun1020.manicule.feature.settings.navigation.settingsScreen
import com.leeseungyun1020.manicule.feature.stats.navigation.statsScreen

/**
 * Manicule 의 최상위 NavHost.
 *
 * 각 feature 모듈이 소유하는 `NavGraphBuilder.<name>Screen()`을 등록한다.
 */
@Composable
fun ManiculeNavHost(
    appState: ManiculeAppState,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
) {
    NavHost(
        navController = appState.navController,
        startDestination = HomeRoute,
        modifier =
            modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
    ) {
        homeScreen(
            onNavigateToSearch = {
                appState.navController.navigate(SearchRoute)
            },
            onNavigateToBookDetail = { isbn ->
                appState.navController.navigate(BookDetailRoute(isbn))
            },
            onNavigateToReadingBooks = {
                appState.navController.navigate(LibraryRoute(LibraryTab.READING))
            },
            onNavigateToWantBooks = {
                appState.navController.navigate(LibraryRoute(LibraryTab.WANT))
            },
            onNavigateToScanner = {
                appState.navController.navigate(ScannerRoute)
            },
            onNavigateToStats = {
                appState.navigateToTopLevelDestination(TopLevelDestination.STATS)
            },
        )
        searchScreen(
            onNavigateBack = {
                appState.navController.popBackStack()
            },
            onBookSelected = { isbn ->
                appState.navController.navigate(BookDetailRoute(isbn))
            },
            scannerAction =
                SearchScannerAction.Available {
                    appState.navController.navigate(ScannerRoute)
                },
        )
        scannerScreen(
            onNavigateBack = { appState.navController.popBackStack() },
            onNavigateToSearch = {
                appState.navController.navigate(SearchRoute) {
                    popUpTo<ScannerRoute> { inclusive = true }
                    launchSingleTop = true
                }
            },
            onNavigateToBookDetail = { isbn ->
                appState.navController.navigate(BookDetailRoute(isbn)) {
                    popUpTo<ScannerRoute> { inclusive = true }
                }
            },
        )
        bookDetailScreen(
            onNavigateBack = { appState.navController.popBackStack() },
        )
        libraryScreen(
            onNavigateToBookDetail = { isbn ->
                appState.navController.navigate(BookDetailRoute(isbn))
            },
            onNavigateToSearch = {
                appState.navController.navigate(SearchRoute)
            },
            onNavigateToScanner = {
                appState.navController.navigate(ScannerRoute)
            },
        )
        statsScreen(
            onBookSelected = { isbn ->
                appState.navController.navigate(BookDetailRoute(isbn, openMyRecords = true))
            },
        )
        settingsScreen()
    }
}
