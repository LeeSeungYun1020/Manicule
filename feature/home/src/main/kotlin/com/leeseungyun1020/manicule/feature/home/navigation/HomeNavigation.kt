package com.leeseungyun1020.manicule.feature.home.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.home.HomeScreen
import com.leeseungyun1020.manicule.feature.home.HomeViewModel
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavGraphBuilder.homeScreen(
    onNavigateToSearch: () -> Unit,
    onNavigateToBookDetail: (isbn: String) -> Unit,
    onNavigateToReadingBooks: () -> Unit,
    onNavigateToWantBooks: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToStats: () -> Unit,
) {
    composable<HomeRoute> {
        HomeRoute(
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToBookDetail = onNavigateToBookDetail,
            onNavigateToReadingBooks = onNavigateToReadingBooks,
            onNavigateToWantBooks = onNavigateToWantBooks,
            onNavigateToScanner = onNavigateToScanner,
            onNavigateToStats = onNavigateToStats,
        )
    }
}

@Composable
private fun HomeRoute(
    onNavigateToSearch: () -> Unit,
    onNavigateToBookDetail: (isbn: String) -> Unit,
    onNavigateToReadingBooks: () -> Unit,
    onNavigateToWantBooks: () -> Unit,
    onNavigateToScanner: () -> Unit,
    onNavigateToStats: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    HomeScreen(
        uiState = uiState,
        onSearch = onNavigateToSearch,
        onScan = onNavigateToScanner,
        onBookSelected = onNavigateToBookDetail,
        onShowReadingBooks = onNavigateToReadingBooks,
        onChooseWantBook = onNavigateToWantBooks,
        onShowStats = onNavigateToStats,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}
