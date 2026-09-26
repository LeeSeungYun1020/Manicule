package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeDialog
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeNetworkErrorState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSnackbarHost
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTabRow
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
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
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import com.leeseungyun1020.manicule.core.designsystem.R as DesignSystemR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    uiState: BookDetailUiState,
    onNavigateBack: () -> Unit,
    onTabSelected: (BookDetailTab) -> Unit,
    onRetry: () -> Unit,
    onStatusSelected: (ReadingStatus) -> Unit,
    onStatusErrorDismissed: () -> Unit,
    onRatingSelected: (Int) -> Unit = {},
    onRatingErrorDismissed: () -> Unit = {},
    onRetryRating: () -> Unit = {},
    onMemoDraftChanged: (String) -> Unit,
    onSaveMemo: () -> Unit,
    onSaveMemoAndCheckSuccess: suspend () -> Boolean,
    onRetryMemo: () -> Unit,
    onMemoErrorDismissed: () -> Unit,
    onAddRecord: (LocalDate, LocalTime, Int, Int) -> Long? = { _, _, _, _ -> null },
    onRecordErrorDismissed: () -> Unit = {},
    onFinishCheckConfirmed: (Long) -> Unit = {},
    onFinishCheckDismissed: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    var isNavigatingBack by rememberSaveable { mutableStateOf(false) }

    val onBackWithSave = {
        if (!isNavigatingBack) {
            focusManager.clearFocus()
            coroutineScope.launch {
                isNavigatingBack = true
                if (onSaveMemoAndCheckSuccess()) {
                    onNavigateBack()
                } else {
                    isNavigatingBack = false
                }
            }
        }
    }

    val onTabSelectedWithSave: (BookDetailTab) -> Unit = { tab ->
        if (!isNavigatingBack) {
            focusManager.clearFocus()
            coroutineScope.launch {
                if (onSaveMemoAndCheckSuccess()) {
                    onTabSelected(tab)
                }
            }
        }
    }

    BackHandler(enabled = true) {
        onBackWithSave()
    }

    val snackbarHostState =
        rememberBookDetailSnackbarHostState(
            content = uiState as? BookDetailUiState.Content,
            onRetry = onRetry,
            onStatusSelected = onStatusSelected,
            onStatusErrorDismissed = onStatusErrorDismissed,
            onRatingErrorDismissed = onRatingErrorDismissed,
            onRetryRating = onRetryRating,
            onRetryMemo = onRetryMemo,
            onMemoErrorDismissed = onMemoErrorDismissed,
            onRecordErrorDismissed = onRecordErrorDismissed,
        )
    var showAddRecordSheet by rememberSaveable { mutableStateOf(false) }
    var pendingRecordSaveAttempt by rememberSaveable { mutableStateOf<Long?>(null) }
    BookDetailRecordSaveEffect(
        recordSaving = (uiState as? BookDetailUiState.Content)?.recordSaving,
        pendingRecordSaveAttempt = pendingRecordSaveAttempt,
        onResetAttempt = { pendingRecordSaveAttempt = null },
        onDismissSheet = { showAddRecordSheet = false },
    )

    val onStatusSelectedWithClear: (ReadingStatus) -> Unit = { status ->
        focusManager.clearFocus()
        onStatusSelected(status)
    }

    val onRatingSelectedWithClear: (Int) -> Unit = { rating ->
        focusManager.clearFocus()
        onRatingSelected(rating)
    }

    val onAddRecordWithClear = {
        focusManager.clearFocus()
        showAddRecordSheet = true
    }

    Scaffold(
        modifier =
            modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Initial)
                            if (event.changes.any { it.pressed }) {
                                focusManager.clearFocus()
                            }
                        }
                    }
                },
        topBar = {
            BookDetailScreenTopBar(
                uiState = uiState,
                scrollBehavior = scrollBehavior,
                onNavigateBack = { onBackWithSave() },
                onTabSelected = onTabSelectedWithSave,
            )
        },
        snackbarHost = { ManiculeSnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        BookDetailBody(
            uiState = uiState,
            onRetry = onRetry,
            onStatusSelected = onStatusSelectedWithClear,
            onRatingSelected = onRatingSelectedWithClear,
            onMemoDraftChanged = onMemoDraftChanged,
            onSaveMemo = onSaveMemo,
            onAddRecord = onAddRecordWithClear,
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        )
    }

    BookDetailOverlays(
        uiState = uiState,
        showAddRecordSheet = showAddRecordSheet,
        onDismissRecordSheet = { showAddRecordSheet = false },
        onAddRecord = onAddRecord,
        onRecordAttemptSaved = { pendingRecordSaveAttempt = it },
        onFinishCheckConfirmed = onFinishCheckConfirmed,
        onFinishCheckDismissed = onFinishCheckDismissed,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailScreenTopBar(
    uiState: BookDetailUiState,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
    onTabSelected: (BookDetailTab) -> Unit,
) {
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
}

@Composable
private fun BookDetailRecordSaveEffect(
    recordSaving: RecordSavingState?,
    pendingRecordSaveAttempt: Long?,
    onResetAttempt: () -> Unit,
    onDismissSheet: () -> Unit,
) {
    LaunchedEffect(recordSaving) {
        when (recordSaving) {
            is RecordSavingState.Saving -> Unit
            is RecordSavingState.Succeeded -> {
                if (pendingRecordSaveAttempt == recordSaving.attempt) {
                    onDismissSheet()
                    onResetAttempt()
                }
            }

            is RecordSavingState.Failed -> onResetAttempt()
            RecordSavingState.Idle, null -> Unit
        }
    }
}

@Composable
private fun BookDetailOverlays(
    uiState: BookDetailUiState,
    showAddRecordSheet: Boolean,
    onDismissRecordSheet: () -> Unit,
    onAddRecord: (LocalDate, LocalTime, Int, Int) -> Long?,
    onRecordAttemptSaved: (Long?) -> Unit,
    onFinishCheckConfirmed: (Long) -> Unit,
    onFinishCheckDismissed: () -> Unit,
) {
    if (showAddRecordSheet && uiState is BookDetailUiState.Content) {
        val initialStartPage = (uiState.records.maxOfOrNull { it.endPage } ?: 0) + 1
        AddRecordBottomSheet(
            initialStartPage = initialStartPage,
            isSaving = uiState.recordSaving is RecordSavingState.Saving,
            onDismissRequest = onDismissRecordSheet,
            onSave = { date, time, startPage, endPage ->
                onRecordAttemptSaved(onAddRecord(date, time, startPage, endPage))
            },
        )
    }

    val finishCheck = (uiState as? BookDetailUiState.Content)?.finishCheck as? FinishCheckState.Active
    if (finishCheck != null) {
        FinishCheckDialog(
            finishCheck = finishCheck,
            onConfirm = onFinishCheckConfirmed,
            onDismiss = onFinishCheckDismissed,
        )
    }
}

@Composable
private fun FinishCheckDialog(
    finishCheck: FinishCheckState.Active,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    val isConfirming = finishCheck is FinishCheckState.Confirming
    ManiculeDialog(
        onDismissRequest = { if (!isConfirming) onDismiss() },
        icon = {
            Icon(
                imageVector = ManiculeIcons.Celebration,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = stringResource(R.string.book_detail_finish_check_title),
        message = stringResource(
            R.string.book_detail_finish_check_message,
            finishCheck.maxEndPage,
            finishCheck.totalPages,
        ),
        confirmText = stringResource(R.string.book_detail_finish_check_confirm),
        dismissText = stringResource(R.string.book_detail_finish_check_dismiss),
        onConfirm = { if (!isConfirming) onConfirm(finishCheck.attempt) },
        onDismiss = { if (!isConfirming) onDismiss() },
    )
}

@Composable
private fun rememberBookDetailSnackbarHostState(
    content: BookDetailUiState.Content?,
    onRetry: () -> Unit,
    onStatusSelected: (ReadingStatus) -> Unit,
    onStatusErrorDismissed: () -> Unit,
    onRatingErrorDismissed: () -> Unit,
    onRetryRating: () -> Unit,
    onRetryMemo: () -> Unit,
    onMemoErrorDismissed: () -> Unit,
    onRecordErrorDismissed: () -> Unit,
): SnackbarHostState {
    val currentOnRetry by rememberUpdatedState(onRetry)
    val currentOnStatusSelected by rememberUpdatedState(onStatusSelected)
    val currentOnStatusErrorDismissed by rememberUpdatedState(onStatusErrorDismissed)
    val currentOnRatingErrorDismissed by rememberUpdatedState(onRatingErrorDismissed)
    val currentOnRetryRating by rememberUpdatedState(onRetryRating)
    val currentOnRetryMemo by rememberUpdatedState(onRetryMemo)
    val currentOnMemoErrorDismissed by rememberUpdatedState(onMemoErrorDismissed)
    val currentOnRecordErrorDismissed by rememberUpdatedState(onRecordErrorDismissed)
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.book_detail_refresh_error)
    val retryActionLabel = stringResource(DesignSystemR.string.core_designsystem_retry)

    val statusErrorMessage = stringResource(R.string.book_detail_status_error)
    val ratingErrorMessage = stringResource(R.string.book_detail_rating_error)
    val memoErrorMessage = stringResource(R.string.book_detail_memo_error)
    val recordErrorMessage = stringResource(R.string.book_detail_record_save_error)
    val recordLoadErrorMessage = stringResource(R.string.book_detail_records_error_title)
    val statusChange = content?.statusChange
    val ratingSaving = content?.ratingSaving
    val memoSaving = content?.memoSaving
    val recordSaving = content?.recordSaving
    val refreshStatus = content?.refreshStatus
    val recordLoadState = content?.recordLoadState
    val hasRecords = content?.records?.isNotEmpty() == true
    LaunchedEffect(refreshStatus, statusChange) {
        if (statusChange is StatusChangeState.Failed) {
            val result =
                snackbarHostState.showSnackbar(
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

    LaunchedEffect(ratingSaving) {
        if (ratingSaving is RatingSavingState.Failed) {
            val result =
                snackbarHostState.showSnackbar(
                    message = ratingErrorMessage,
                    actionLabel = retryActionLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
            if (result == SnackbarResult.ActionPerformed) {
                currentOnRetryRating()
            } else {
                currentOnRatingErrorDismissed()
            }
        }
    }

    LaunchedEffect(memoSaving) {
        if (memoSaving is MemoSavingState.Failed) {
            val result =
                snackbarHostState.showSnackbar(
                    message = memoErrorMessage,
                    actionLabel = retryActionLabel,
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
            if (result == SnackbarResult.ActionPerformed) {
                currentOnRetryMemo()
            } else {
                currentOnMemoErrorDismissed()
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
    onRatingSelected: (Int) -> Unit,
    onMemoDraftChanged: (String) -> Unit,
    onSaveMemo: () -> Unit,
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
                            rating = uiState.bookDetail.entry?.rating ?: 0,
                            memo = uiState.bookDetail.entry?.memo,
                            memoDraft = uiState.memoDraft,
                            isRatingSaving = uiState.ratingSaving is RatingSavingState.Saving,
                            isMemoSaving = uiState.memoSaving is MemoSavingState.Saving,
                            records = uiState.records,
                            recordLoadState = uiState.recordLoadState,
                            totalPages = uiState.bookDetail.book.totalPages,
                            onStatusSelected = onStatusSelected,
                            onRatingSelected = onRatingSelected,
                            onMemoDraftChanged = onMemoDraftChanged,
                            onSaveMemo = onSaveMemo,
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

@Composable
private fun PreviewBookDetailScreen(
    uiState: BookDetailUiState,
    modifier: Modifier = Modifier,
) {
    BookDetailScreen(
        uiState = uiState,
        onNavigateBack = {},
        onTabSelected = {},
        onRetry = {},
        onStatusSelected = {},
        onStatusErrorDismissed = {},
        onMemoDraftChanged = {},
        onSaveMemo = {},
        onSaveMemoAndCheckSuccess = { true },
        onRetryMemo = {},
        onMemoErrorDismissed = {},
        onAddRecord = { _, _, _, _ -> null },
        onRecordErrorDismissed = {},
        onFinishCheckConfirmed = {},
        onFinishCheckDismissed = {},
        modifier = modifier,
    )
}

@ManiculePreview
@Preview(name = "Phone", device = Devices.PHONE)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
private fun BookDetailScreenPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = null),
                    selectedTab = BookDetailTab.Information,
                ),
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailLoadingPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState = BookDetailUiState.Loading,
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailErrorPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState = BookDetailUiState.Error,
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailRefreshErrorPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = null),
                    selectedTab = BookDetailTab.Information,
                    refreshStatus = RefreshStatus.Failed,
                ),
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailRefreshingPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = null),
                    selectedTab = BookDetailTab.Information,
                    refreshStatus = RefreshStatus.Refreshing,
                ),
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailReviewOnlyPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = previewReviewOnlyEntry),
                    selectedTab = BookDetailTab.MyRecords,
                ),
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailFinishCheckPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = null),
                    selectedTab = BookDetailTab.MyRecords,
                    finishCheck = FinishCheckState.Pending(attempt = 1L, maxEndPage = 254, totalPages = 264),
                ),
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailMemoEditingPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = previewReviewOnlyEntry),
                    selectedTab = BookDetailTab.MyRecords,
                    memoDraft = "수정 중인 메모 초안",
                ),
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailMemoSavingPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = previewReviewOnlyEntry),
                    selectedTab = BookDetailTab.MyRecords,
                    memoDraft = "저장 중인 메모",
                    memoSaving = MemoSavingState.Saving("저장 중인 메모"),
                ),
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailMemoFailedPreview() {
    ManiculePreviewTheme {
        PreviewBookDetailScreen(
            uiState =
                BookDetailUiState.Content(
                    bookDetail = BookDetail(previewBook, entry = previewReviewOnlyEntry),
                    selectedTab = BookDetailTab.MyRecords,
                    memoDraft = "저장 실패한 메모 초안",
                    memoSaving = MemoSavingState.Failed("저장 실패한 메모 초안", attempt = 1L),
                ),
        )
    }
}
