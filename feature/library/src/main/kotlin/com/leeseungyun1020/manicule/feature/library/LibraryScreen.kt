package com.leeseungyun1020.manicule.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeOutlinedButton
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.library.components.LibraryBookCard
import com.leeseungyun1020.manicule.feature.library.components.LibraryTopBar
import com.leeseungyun1020.manicule.feature.library.components.SortBottomSheet
import kotlinx.datetime.Instant

private const val LIBRARY_BOOK_CONTENT_TYPE = "library_book"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    uiState: LibraryUiState,
    onStatusSelected: (ReadingStatus) -> Unit,
    onSortSelected: (LibrarySort) -> Unit,
    onBookSelected: (String) -> Unit,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSortSheet by rememberSaveable { mutableStateOf(false) }
    var draftSortCriterion by rememberSaveable { mutableStateOf(uiState.sort.criterion) }
    var draftSortDirection by rememberSaveable { mutableStateOf(uiState.sort.direction) }

    LibraryScaffold(
        uiState = uiState,
        onStatusSelected = onStatusSelected,
        onSortClick = {
            draftSortCriterion = uiState.sort.criterion
            draftSortDirection = uiState.sort.direction
            showSortSheet = true
        },
        onBookSelected = onBookSelected,
        onSearch = onSearch,
        onScan = onScan,
        onRetry = onRetry,
        modifier = modifier,
    )

    if (showSortSheet) {
        val draftSort = LibrarySort(draftSortCriterion, draftSortDirection)
        SortBottomSheet(
            sort = draftSort,
            onSortChange = {
                draftSortCriterion = it.criterion
                draftSortDirection = it.direction
            },
            onDismissRequest = { showSortSheet = false },
            onApply = {
                onSortSelected(draftSort)
                showSortSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryScaffold(
    uiState: LibraryUiState,
    onStatusSelected: (ReadingStatus) -> Unit,
    onSortClick: () -> Unit,
    onBookSelected: (String) -> Unit,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val hasBooks = uiState is LibraryUiState.Content && uiState.books.isNotEmpty()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LibraryTopBar(
                selectedStatus = uiState.selectedStatus,
                sort = uiState.sort,
                hasBooks = hasBooks,
                onStatusSelected = onStatusSelected,
                onSortClick = onSortClick,
                onSearch = onSearch,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        LibraryBody(uiState, contentPadding, onBookSelected, onSearch, onScan, onRetry)
    }
}

@Composable
private fun LibraryBody(
    uiState: LibraryUiState,
    contentPadding: PaddingValues,
    onBookSelected: (String) -> Unit,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onRetry: () -> Unit,
) {
    when (uiState) {
        is LibraryUiState.Loading -> ManiculeLoading(Modifier.fillMaxSize().padding(contentPadding))
        is LibraryUiState.Error -> LibraryError(contentPadding, onRetry)
        is LibraryUiState.Content -> {
            if (uiState.books.isEmpty()) {
                EmptyLibrary(contentPadding, onSearch, onScan)
            } else {
                LibraryGrid(contentPadding, uiState.books, onBookSelected)
            }
        }
    }
}

@Composable
private fun LibraryGrid(
    scaffoldPadding: PaddingValues,
    books: List<BookEntry>,
    onBookSelected: (String) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(ManiculeSize.coverMediumWidth),
        modifier = Modifier.fillMaxSize().padding(scaffoldPadding),
        contentPadding = ManiculeSpacing.screenContent,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        items(
            items = books,
            key = { it.book.isbn },
            contentType = { LIBRARY_BOOK_CONTENT_TYPE },
        ) { entry ->
            LibraryBookCard(
                book = entry.book,
                onClick = { onBookSelected(entry.book.isbn) },
            )
        }
    }
}

@Composable
private fun EmptyLibrary(
    scaffoldPadding: PaddingValues,
    onSearch: () -> Unit,
    onScan: () -> Unit,
) {
    ManiculeEmptyState(
        title = stringResource(R.string.library_empty_title),
        description = stringResource(R.string.library_empty_description),
        modifier = Modifier.fillMaxSize().padding(scaffoldPadding).padding(ManiculeSpacing.screenContent),
        icon = {
            Icon(
                imageVector = ManiculeIcons.Tab.LibraryFilled,
                contentDescription = null,
                modifier = Modifier.size(ManiculeSize.iconEmptyState).testTag("library_empty_icon"),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        actions = {
            ManiculeButton(
                onClick = onSearch,
                text = stringResource(R.string.library_search),
                leadingIcon = {
                    Icon(
                        imageVector = ManiculeIcons.Search,
                        contentDescription = null,
                        modifier = Modifier.testTag("library_search_icon"),
                    )
                },
            )
            ManiculeOutlinedButton(
                onClick = onScan,
                text = stringResource(R.string.library_scan),
                leadingIcon = {
                    Icon(
                        imageVector = ManiculeIcons.ScanBarcode,
                        contentDescription = null,
                        modifier = Modifier.testTag("library_scan_icon"),
                    )
                },
            )
        },
    )
}

@Composable
private fun LibraryError(
    scaffoldPadding: PaddingValues,
    onRetry: () -> Unit,
) {
    ManiculeEmptyState(
        title = stringResource(R.string.library_error_title),
        description = stringResource(R.string.library_error_description),
        modifier = Modifier.fillMaxSize().padding(scaffoldPadding).padding(ManiculeSpacing.screenContent),
        actions = {
            ManiculeButton(onClick = onRetry, text = stringResource(R.string.library_retry))
        },
    )
}

@ManiculePreview
@Preview(name = "Foldable", widthDp = 673, heightDp = 900, showBackground = true)
@Preview(name = "Tablet", widthDp = 1200, heightDp = 900, showBackground = true)
@Composable
private fun LibraryContentPreview() {
    ManiculePreviewTheme {
        LibraryScreen(
            uiState = LibraryUiState.Content(ReadingStatus.READING, previewEntries),
            onStatusSelected = {},
            onSortSelected = {},
            onBookSelected = {},
            onSearch = {},
            onScan = {},
            onRetry = {},
        )
    }
}

@ManiculePreview
@Composable
private fun LibraryEmptyPreview() {
    ManiculePreviewTheme {
        LibraryScreen(
            uiState = LibraryUiState.Content(ReadingStatus.WANT, emptyList()),
            onStatusSelected = {},
            onSortSelected = {},
            onBookSelected = {},
            onSearch = {},
            onScan = {},
            onRetry = {},
        )
    }
}

@ManiculePreview
@Composable
private fun LibraryLoadingPreview() {
    ManiculePreviewTheme {
        LibraryScreen(
            uiState = LibraryUiState.Loading(ReadingStatus.READING),
            onStatusSelected = {},
            onSortSelected = {},
            onBookSelected = {},
            onSearch = {},
            onScan = {},
            onRetry = {},
        )
    }
}

@ManiculePreview
@Composable
private fun LibraryErrorPreview() {
    ManiculePreviewTheme {
        LibraryScreen(
            uiState = LibraryUiState.Error(ReadingStatus.FINISHED),
            onStatusSelected = {},
            onSortSelected = {},
            onBookSelected = {},
            onSearch = {},
            onScan = {},
            onRetry = {},
        )
    }
}

private val previewEntries =
    buildList {
        repeat(7) {
            add(
                BookEntry(
                    book =
                        Book(
                            isbn = "978000000000$it",
                            title = "긴 책 제목도 두 줄 안에서 읽을 수 있는 서재 카드",
                            author = "작가",
                            publisher = "출판사",
                            publishedDate = null,
                            coverUrl = null,
                            totalPages = 320,
                            price = null,
                            category = null,
                            tableOfContentsUrl = null,
                            introductionUrl = null,
                            summaryUrl = null,
                        ),
                    status = ReadingStatus.READING,
                    addedAt = Instant.fromEpochMilliseconds(0),
                    updatedAt = Instant.fromEpochMilliseconds(1),
                ),
            )
        }
    }
