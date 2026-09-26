package com.leeseungyun1020.manicule.feature.bookdetail.navigation

import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.bookdetail.BookDetailScreen
import com.leeseungyun1020.manicule.feature.bookdetail.BookDetailViewModel

typealias BookDetailRoute = com.leeseungyun1020.manicule.feature.bookdetail.BookDetailRoute

fun NavGraphBuilder.bookDetailScreen(onNavigateBack: () -> Unit) {
    composable<BookDetailRoute> {
        val viewModel: BookDetailViewModel = hiltViewModel()
        BookDetailScreen(
            uiState = viewModel.uiState.collectAsStateWithLifecycle().value,
            onNavigateBack = onNavigateBack,
            onTabSelected = viewModel::selectTab,
            onRetry = viewModel::retry,
            onStatusSelected = viewModel::changeReadingStatus,
            onStatusErrorDismissed = viewModel::dismissStatusError,
            onRatingSelected = viewModel::updateRating,
            onRatingErrorDismissed = viewModel::dismissRatingError,
            onRetryRating = viewModel::retryRating,
            onMemoDraftChanged = viewModel::updateMemoDraft,
            onSaveMemo = viewModel::saveMemo,
            onSaveMemoAndCheckSuccess = viewModel::saveMemoAndCheckSuccess,
            onRetryMemo = viewModel::retryMemo,
            onMemoErrorDismissed = viewModel::dismissMemoError,
            onAddRecord = viewModel::addRecord,
            onRecordErrorDismissed = viewModel::dismissRecordError,
            onFinishCheckConfirmed = viewModel::confirmFinish,
            onFinishCheckDismissed = viewModel::dismissFinishCheck,
        )
    }
}
