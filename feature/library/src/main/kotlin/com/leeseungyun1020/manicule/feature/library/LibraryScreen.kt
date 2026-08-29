package com.leeseungyun1020.manicule.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeOutlinedButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTabRow
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.library.components.LibraryBookCard
import kotlinx.datetime.Instant

private val libraryStatuses = listOf(ReadingStatus.WANT, ReadingStatus.READING, ReadingStatus.FINISHED)
private const val LIBRARY_COLUMN_COUNT = 3
private const val LIBRARY_BOOK_CONTENT_TYPE = "library_book"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongParameterList")
fun LibraryScreen(
    uiState: LibraryUiState,
    onStatusSelected: (ReadingStatus) -> Unit,
    onBookSelected: (String) -> Unit,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val wantTabLabel = stringResource(R.string.library_tab_want)
    val readingTabLabel = stringResource(R.string.library_tab_reading)
    val finishedTabLabel = stringResource(R.string.library_tab_finished)
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            Column {
                ManiculeTopAppBar(
                    title = stringResource(R.string.library_title),
                    scrollBehavior = scrollBehavior,
                )
                ManiculeTabRow(
                    tabs = libraryStatuses,
                    selectedTabIndex = libraryStatuses.indexOf(uiState.selectedStatus),
                    onTabSelected = { onStatusSelected(libraryStatuses[it]) },
                    tabLabel = { status ->
                        when (status) {
                            ReadingStatus.WANT -> wantTabLabel
                            ReadingStatus.READING -> readingTabLabel
                            ReadingStatus.FINISHED -> finishedTabLabel
                        }
                    },
                )
                if (uiState is LibraryUiState.Content && uiState.books.isNotEmpty()) {
                    LibraryActionRow(onSearch = onSearch)
                }
            }
        },
    ) { contentPadding ->
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
}

@Composable
private fun LibraryActionRow(onSearch: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.lg),
        horizontalArrangement = Arrangement.End,
    ) {
        ManiculeIconButton(onClick = onSearch) {
            Icon(
                imageVector = ManiculeIcons.Add,
                contentDescription = stringResource(R.string.library_add_book),
            )
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
        columns = GridCells.Fixed(LIBRARY_COLUMN_COUNT),
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
@Composable
private fun LibraryContentPreview() {
    ManiculePreviewTheme {
        LibraryScreen(
            uiState = LibraryUiState.Content(ReadingStatus.READING, previewEntries),
            onStatusSelected = {},
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
            onBookSelected = {},
            onSearch = {},
            onScan = {},
            onRetry = {},
        )
    }
}

private val previewEntries =
    listOf(
        BookEntry(
            book =
                Book(
                    isbn = "9780000000001",
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
