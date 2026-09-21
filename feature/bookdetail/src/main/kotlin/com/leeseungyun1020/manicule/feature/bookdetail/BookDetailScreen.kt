package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeNetworkErrorState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSnackbarHost
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTabRow
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookDetail
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.bookdetail.components.AddRecordBottomSheet
import com.leeseungyun1020.manicule.feature.bookdetail.components.BookInfoTabContent
import com.leeseungyun1020.manicule.feature.bookdetail.components.MyRecordTabContent
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import com.leeseungyun1020.manicule.core.designsystem.R as DesignSystemR

@OptIn(ExperimentalMaterial3Api::class)
// 화면 이벤트를 명시적인 콜백으로 노출한다 (LibraryScreen과 동일).
@Composable
fun BookDetailScreen(
    uiState: BookDetailUiState,
    onNavigateBack: () -> Unit,
    onTabSelected: (BookDetailTab) -> Unit,
    onRetry: () -> Unit,
    onStatusSelected: (ReadingStatus) -> Unit,
    onStatusErrorDismissed: () -> Unit,
    onAddRecord: (LocalDate, LocalTime, Int, Int) -> Long?,
    onRecordErrorDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = rememberBookDetailSnackbarHostState(
        content = uiState as? BookDetailUiState.Content,
        onRetry = onRetry,
        onStatusSelected = onStatusSelected,
        onStatusErrorDismissed = onStatusErrorDismissed,
        onRecordErrorDismissed = onRecordErrorDismissed,
    )
    var showAddRecordSheet by rememberSaveable { mutableStateOf(false) }
    var pendingRecordSaveAttempt by rememberSaveable { mutableStateOf<Long?>(null) }
    val recordSaving = (uiState as? BookDetailUiState.Content)?.recordSaving

    LaunchedEffect(recordSaving) {
        when (recordSaving) {
            is RecordSavingState.Saving -> Unit
            is RecordSavingState.Succeeded -> {
                if (pendingRecordSaveAttempt == recordSaving.attempt) {
                    showAddRecordSheet = false
                    pendingRecordSaveAttempt = null
                }
            }

            is RecordSavingState.Failed -> pendingRecordSaveAttempt = null
            RecordSavingState.Idle, null -> Unit
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                ManiculeTopAppBar(
                    title = if (uiState is BookDetailUiState.Content) uiState.bookDetail.book.title else "",
                    onNavigateBack = onNavigateBack,
                    scrollBehavior = scrollBehavior,
                )
                if (uiState is BookDetailUiState.Content) {
                    BookDetailTab(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = onTabSelected,
                    )
                }
            }
        },
        snackbarHost = { ManiculeSnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        BookDetailBody(
            uiState = uiState,
            onRetry = onRetry,
            onStatusSelected = onStatusSelected,
            onAddRecord = { showAddRecordSheet = true },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        )
    }

    if (showAddRecordSheet && uiState is BookDetailUiState.Content) {
        val initialStartPage = (uiState.records.maxOfOrNull { it.endPage } ?: 0) + 1
        AddRecordBottomSheet(
            initialStartPage = initialStartPage,
            isSaving = uiState.recordSaving is RecordSavingState.Saving,
            onDismissRequest = { showAddRecordSheet = false },
            onSave = { date, time, startPage, endPage ->
                pendingRecordSaveAttempt = onAddRecord(date, time, startPage, endPage)
            },
        )
    }
}

@Composable
private fun rememberBookDetailSnackbarHostState(
    content: BookDetailUiState.Content?,
    onRetry: () -> Unit,
    onStatusSelected: (ReadingStatus) -> Unit,
    onStatusErrorDismissed: () -> Unit,
    onRecordErrorDismissed: () -> Unit,
): SnackbarHostState {
    val currentOnRetry by rememberUpdatedState(onRetry)
    val currentOnStatusSelected by rememberUpdatedState(onStatusSelected)
    val currentOnStatusErrorDismissed by rememberUpdatedState(onStatusErrorDismissed)
    val currentOnRecordErrorDismissed by rememberUpdatedState(onRecordErrorDismissed)
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.book_detail_refresh_error)
    val retryActionLabel = stringResource(DesignSystemR.string.core_designsystem_retry)

    val statusErrorMessage = stringResource(R.string.book_detail_status_error)
    val recordErrorMessage = stringResource(R.string.book_detail_record_save_error)
    val recordLoadErrorMessage = stringResource(R.string.book_detail_records_error_title)
    val statusChange = content?.statusChange
    val recordSaving = content?.recordSaving
    val refreshStatus = content?.refreshStatus
    val recordLoadState = content?.recordLoadState
    val hasRecords = content?.records?.isNotEmpty() == true
    LaunchedEffect(refreshStatus, statusChange) {
        if (statusChange is StatusChangeState.Failed) {
            val result = snackbarHostState.showSnackbar(
                message = statusErrorMessage,
                actionLabel = retryActionLabel,
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite,
            )
            if (result == SnackbarResult.ActionPerformed) {
                currentOnStatusSelected(statusChange.target)
            } else {
                currentOnStatusErrorDismissed()
            }
        } else if (refreshStatus == RefreshStatus.Failed && statusChange !is StatusChangeState.Saving) {
            val result =
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = retryActionLabel,
                    duration = SnackbarDuration.Indefinite,
                )
            if (result == SnackbarResult.ActionPerformed) {
                currentOnRetry()
            }
        }
    }

    LaunchedEffect(recordSaving) {
        if (recordSaving is RecordSavingState.Failed) {
            snackbarHostState.showSnackbar(
                message = recordErrorMessage,
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
            currentOnRecordErrorDismissed()
        }
    }

    LaunchedEffect(recordLoadState, hasRecords) {
        if (recordLoadState is RecordLoadState.Failed && hasRecords) {
            val result =
                snackbarHostState.showSnackbar(
                    message = recordLoadErrorMessage,
                    actionLabel = retryActionLabel,
                    duration = SnackbarDuration.Indefinite,
                )
            if (result == SnackbarResult.ActionPerformed) {
                currentOnRetry()
            }
        }
    }

    return snackbarHostState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailTab(
    selectedTab: BookDetailTab,
    onTabSelected: (BookDetailTab) -> Unit,
) {
    val tabs =
        listOf(
            stringResource(R.string.book_detail_tab_information),
            stringResource(R.string.book_detail_tab_my_records),
        )
    ManiculeTabRow(
        tabs = tabs,
        selectedTabIndex = selectedTab.ordinal,
        onTabSelected = { index -> onTabSelected(BookDetailTab.entries[index]) },
    )
}

@Composable
private fun BookDetailBody(
    uiState: BookDetailUiState,
    onRetry: () -> Unit,
    onStatusSelected: (ReadingStatus) -> Unit,
    onAddRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when (uiState) {
            is BookDetailUiState.Loading -> ManiculeLoading(modifier = Modifier.fillMaxSize())
            is BookDetailUiState.Error ->
                BookDetailError(
                    onRetry = onRetry,
                )

            is BookDetailUiState.Content -> {
                when (uiState.selectedTab) {
                    BookDetailTab.Information ->
                        BookInfoTabContent(book = uiState.bookDetail.book)

                    BookDetailTab.MyRecords ->
                        MyRecordTabContent(
                            status = uiState.bookDetail.entry?.status,
                            isSaving = uiState.statusChange is StatusChangeState.Saving,
                            records = uiState.records,
                            recordLoadState = uiState.recordLoadState,
                            totalPages = uiState.bookDetail.book.totalPages,
                            onStatusSelected = onStatusSelected,
                            onAddRecord = onAddRecord,
                            onRetryRecords = onRetry,
                        )
                }
            }
        }
    }
}

@Composable
private fun BookDetailError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManiculeNetworkErrorState(
        onRetry = onRetry,
        modifier =
            modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.lg),
    )
}

private val previewBook =
    Book(
        isbn = "9791161759692",
        title = "Kotlin in Action 2/e",
        author = "세바스티안 아이그너 외",
        publisher = "에이콘출판사",
        publishedDate = LocalDate(2025, 2, 27),
        coverUrl = null,
        totalPages = 803,
        price = 48_000,
        category = "프로그래밍",
        tableOfContentsUrl = null,
        introductionUrl = null,
        summaryUrl = null,
        introduction = "코틀린 언어와 실전 개발 패턴을 소개합니다. ".repeat(8),
        tableOfContents = "1장 코틀린이란 무엇이며 왜 필요한가\n2장 코틀린 기초",
    )

private val previewReviewOnlyEntry =
    BookEntry(
        book = previewBook,
        status = ReadingStatus.UNSET,
        rating = 4,
        memo = "독서 상태를 정하지 않고 남긴 리뷰",
        addedAt = Instant.fromEpochMilliseconds(1),
        updatedAt = Instant.fromEpochMilliseconds(1),
    )

@ManiculePreview
@Preview(name = "Phone", device = Devices.PHONE)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
private fun BookDetailScreenPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = null),
                    selectedTab = BookDetailTab.Information,
                ),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
            onStatusSelected = {},
            onStatusErrorDismissed = {},
            onAddRecord = { _, _, _, _ -> null },
            onRecordErrorDismissed = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailLoadingPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState = BookDetailUiState.Loading,
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
            onStatusSelected = {},
            onStatusErrorDismissed = {},
            onAddRecord = { _, _, _, _ -> null },
            onRecordErrorDismissed = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailErrorPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState = BookDetailUiState.Error,
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
            onStatusSelected = {},
            onStatusErrorDismissed = {},
            onAddRecord = { _, _, _, _ -> null },
            onRecordErrorDismissed = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailRefreshErrorPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = null),
                    selectedTab = BookDetailTab.Information,
                    refreshStatus = RefreshStatus.Failed,
                ),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
            onStatusSelected = {},
            onStatusErrorDismissed = {},
            onAddRecord = { _, _, _, _ -> null },
            onRecordErrorDismissed = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailRefreshingPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = null),
                    selectedTab = BookDetailTab.Information,
                    refreshStatus = RefreshStatus.Refreshing,
                ),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
            onStatusSelected = {},
            onStatusErrorDismissed = {},
            onAddRecord = { _, _, _, _ -> null },
            onRecordErrorDismissed = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailReviewOnlyPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = previewReviewOnlyEntry),
                    selectedTab = BookDetailTab.MyRecords,
                ),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
            onStatusSelected = {},
            onStatusErrorDismissed = {},
            onAddRecord = { _, _, _, _ -> null },
            onRecordErrorDismissed = {},
        )
    }
}
