package com.leeseungyun1020.manicule.feature.search.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeNetworkErrorState
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.ui.preview.BookPreviewParameterProvider
import com.leeseungyun1020.manicule.feature.search.R
import com.leeseungyun1020.manicule.feature.search.SearchScannerAction
import kotlinx.coroutines.flow.flowOf

@Composable
fun SearchResultList(
    books: LazyPagingItems<Book>,
    listState: LazyListState,
    onBookSelected: (String) -> Unit,
    scannerAction: SearchScannerAction,
    modifier: Modifier = Modifier,
) {
    when (books.loadState.refresh) {
        is LoadState.Loading ->
            SearchResultLoading(modifier = modifier)

        is LoadState.Error ->
            SearchResultError(
                onRetry = books::retry,
                modifier = modifier,
            )

        is LoadState.NotLoading ->
            if (books.itemCount == 0) {
                EmptySearchResult(scannerAction = scannerAction, modifier = modifier)
            } else {
                SearchResultContent(
                    books = books,
                    listState = listState,
                    onBookSelected = onBookSelected,
                    modifier = modifier,
                )
            }
    }
}

@Composable
private fun SearchResultContent(
    books: LazyPagingItems<Book>,
    listState: LazyListState,
    onBookSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize().testTag("search_results"),
    ) {
        item(
            key = "search_result_header",
            contentType = "section_header",
        ) {
            Text(
                text = stringResource(R.string.search_result_title),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier =
                    Modifier.padding(
                        horizontal = MaterialTheme.spacing.sm,
                        vertical = MaterialTheme.spacing.sm,
                    ),
            )
        }

        items(
            count = books.itemCount,
            key = { index ->
                "$index:${books.peek(index)?.isbn.orEmpty()}"
            },
            contentType = { "book" },
        ) { index ->
            books[index]?.let { book ->
                SearchResultItem(
                    book = book,
                    onBookSelected = onBookSelected,
                    showDivider = index < books.itemCount - 1,
                )
            }
        }

        when (val appendState = books.loadState.append) {
            is LoadState.Loading,
            is LoadState.Error,
            ->
                item(
                    key = "append_state",
                    contentType = "append_state",
                ) {
                    SearchAppendState(
                        loadState = appendState,
                        onRetry = books::retry,
                    )
                }

            is LoadState.NotLoading -> Unit
        }
    }
}

@Composable
private fun SearchResultLoading(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.search_result_loading)
    ManiculeLoading(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { contentDescription = description },
    )
}

@Composable
private fun SearchResultError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManiculeNetworkErrorState(
        onRetry = onRetry,
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
private fun EmptySearchResult(
    scannerAction: SearchScannerAction,
    modifier: Modifier = Modifier,
) {
    ManiculeEmptyState(
        title = stringResource(R.string.search_result_empty_title),
        description = stringResource(R.string.search_result_empty_description),
        modifier = modifier.fillMaxSize(),
        icon = {
            Icon(
                imageVector = ManiculeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(ManiculeSize.iconEmptyState),
            )
        },
        actions = {
            ManiculeButton(
                text = stringResource(R.string.search_scan),
                enabled = scannerAction is SearchScannerAction.Available,
                onClick = {
                    if (scannerAction is SearchScannerAction.Available) scannerAction.onNavigate()
                },
                leadingIcon = {
                    Icon(imageVector = ManiculeIcons.ScanBarcode, contentDescription = null)
                },
            )
        },
    )
}

@ManiculePreview
@Composable
private fun SearchResultContentPreview() {
    val books =
        flowOf(
            PagingData.from(
                BookPreviewParameterProvider().values.toList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(false),
                    prepend = LoadState.NotLoading(true),
                    append = LoadState.NotLoading(true),
                ),
            ),
        ).collectAsLazyPagingItems()

    ManiculePreviewTheme {
        SearchResultList(
            books = books,
            listState = rememberLazyListState(),
            onBookSelected = {},
            scannerAction = SearchScannerAction.Unavailable,
        )
    }
}

@ManiculePreview
@Composable
private fun SearchResultLoadingPreview() {
    ManiculePreviewTheme {
        SearchResultLoading()
    }
}

@ManiculePreview
@Composable
private fun SearchResultErrorPreview() {
    ManiculePreviewTheme {
        SearchResultError(onRetry = {})
    }
}

@ManiculePreview
@Composable
private fun EmptySearchResultPreview() {
    ManiculePreviewTheme {
        EmptySearchResult(scannerAction = SearchScannerAction.Unavailable)
    }
}

@ManiculePreview
@Composable
private fun EmptySearchResultWithScannerPreview() {
    ManiculePreviewTheme {
        EmptySearchResult(scannerAction = SearchScannerAction.Available {})
    }
}
