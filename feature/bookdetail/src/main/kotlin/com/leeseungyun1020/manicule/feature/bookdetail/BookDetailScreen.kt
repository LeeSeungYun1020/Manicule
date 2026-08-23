package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSnackbarHost
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTabRow
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.feature.bookdetail.components.BookInfoTabContent
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    uiState: BookDetailUiState,
    onNavigateBack: () -> Unit,
    onTabSelected: (BookDetailTab) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.book_detail_refresh_error)
    val retryActionLabel = stringResource(R.string.book_detail_retry)

    LaunchedEffect(uiState.isRefreshError) {
        if (uiState.isRefreshError) {
            val result =
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = retryActionLabel,
                    duration = SnackbarDuration.Indefinite,
                )
            if (result == SnackbarResult.ActionPerformed) {
                onRetry()
            }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BookDetailTopBar(
                title = uiState.book?.title ?: stringResource(R.string.book_detail_title),
                selectedTab = uiState.selectedTab,
                onNavigateBack = onNavigateBack,
                onTabSelected = onTabSelected,
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { ManiculeSnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        BookDetailBody(
            uiState = uiState,
            onRetry = onRetry,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookDetailTopBar(
    title: String,
    selectedTab: BookDetailTab,
    onNavigateBack: () -> Unit,
    onTabSelected: (BookDetailTab) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val tabs =
        listOf(
            stringResource(R.string.book_detail_tab_information),
            stringResource(R.string.book_detail_tab_my_records),
        )
    Column {
        ManiculeTopAppBar(
            title = title,
            onNavigateBack = onNavigateBack,
            scrollBehavior = scrollBehavior,
        )
        ManiculeTabRow(
            tabs = tabs,
            selectedTabIndex = selectedTab.ordinal,
            onTabSelected = { index -> onTabSelected(BookDetailTab.entries[index]) },
        )
    }
}

@Composable
private fun BookDetailBody(
    uiState: BookDetailUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        when {
            uiState.isLoading -> ManiculeLoading(modifier = Modifier.fillMaxSize())
            uiState.isFatalError || uiState.book == null ->
                ManiculeEmptyState(
                    title = stringResource(R.string.book_detail_error_title),
                    description = stringResource(R.string.book_detail_error_description),
                    modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.lg),
                    icon = {
                        Icon(
                            imageVector = ManiculeIcons.NetworkError,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(MaterialTheme.size.iconEmptyState),
                        )
                    },
                    actions = { ManiculeButton(onClick = onRetry, text = stringResource(R.string.book_detail_retry)) },
                )
            uiState.selectedTab == BookDetailTab.Information ->
                BookInfoTabContent(book = uiState.book)
            else ->
                Text(
                    text = stringResource(R.string.book_detail_records_stub),
                    style = MaterialTheme.typography.bodyLarge,
                )
        }
    }
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

@ManiculePreview
@Preview(name = "Phone", device = Devices.PHONE)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
private fun BookDetailScreenPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState = BookDetailUiState(book = previewBook, isLoading = false),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailLoadingPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState = BookDetailUiState(),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailErrorPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState = BookDetailUiState(isLoading = false, isFatalError = true),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailRefreshErrorPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState = BookDetailUiState(book = previewBook, isLoading = false, isRefreshError = true),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailRecordsStubPreview() {
    ManiculePreviewTheme {
        BookDetailScreen(
            uiState =
                BookDetailUiState(
                    book = previewBook,
                    isLoading = false,
                    selectedTab = BookDetailTab.MyRecords,
                ),
            onNavigateBack = {},
            onTabSelected = {},
            onRetry = {},
        )
    }
}
