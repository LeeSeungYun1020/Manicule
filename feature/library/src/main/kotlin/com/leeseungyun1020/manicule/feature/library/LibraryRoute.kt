package com.leeseungyun1020.manicule.feature.library

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LibraryRoute(
    onNavigateToBookDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToScanner: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    LibraryScreen(
        uiState = uiState,
        onStatusSelected = viewModel::selectStatus,
        onBookSelected = onNavigateToBookDetail,
        onSearch = onNavigateToSearch,
        onScan = onNavigateToScanner,
        onRetry = viewModel::retry,
    )
}
