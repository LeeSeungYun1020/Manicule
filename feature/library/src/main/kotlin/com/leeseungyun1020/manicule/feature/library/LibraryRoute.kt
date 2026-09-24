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
    val actionMessage = viewModel.actionMessage.collectAsStateWithLifecycle().value
    LibraryScreen(
        uiState = uiState,
        actionMessage = actionMessage,
        onStatusSelected = viewModel::selectStatus,
        onSortSelected = viewModel::selectSort,
        onBookSelected = onNavigateToBookDetail,
        onChangeStatus = viewModel::changeStatus,
        onDeleteBook = viewModel::deleteBook,
        onUndo = viewModel::undo,
        onMessageDismissed = viewModel::messageDismissed,
        onSearch = onNavigateToSearch,
        onScan = onNavigateToScanner,
        onRetry = viewModel::retry,
    )
}
