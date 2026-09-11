package com.leeseungyun1020.manicule.feature.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTextButton
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.ui.book.BookListItem
import com.leeseungyun1020.manicule.core.ui.preview.BookPreviewParameterProvider
import com.leeseungyun1020.manicule.feature.search.R
import kotlinx.coroutines.flow.flowOf

@Composable
fun SearchResultList(
    searchRequestId: Long?,
    books: LazyPagingItems<Book>,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(searchRequestId) {
        listState.scrollToItem(0)
    }

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
                EmptySearchResult(modifier = modifier)
            } else {
                SearchResultContent(
                    books = books,
                    listState = listState,
                    modifier = modifier,
                )
            }
    }
}

@Composable
private fun SearchResultContent(
    books: LazyPagingItems<Book>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
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
                BookListItem(
                    title = book.title,
                    author = book.author,
                    publisher = book.publisher,
                    pubDate = book.publishedDate?.toString().orEmpty(),
                    imageUrl = book.coverUrl,
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
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(MaterialTheme.spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = ManiculeIcons.NetworkError,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(ManiculeSize.iconEmptyState),
        )
        Text(
            text = stringResource(R.string.search_result_error_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
        )
        Text(
            text = stringResource(R.string.search_result_error_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
        )
        ManiculeButton(
            onClick = onRetry,
            text = stringResource(R.string.search_retry),
            modifier = Modifier.padding(top = MaterialTheme.spacing.md),
        )
    }
}

@Composable
private fun EmptySearchResult(modifier: Modifier = Modifier) {
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
    )
}

@Composable
private fun SearchAppendState(
    loadState: LoadState,
    onRetry: () -> Unit,
) {
    when (loadState) {
        is LoadState.Loading -> {
            val description = stringResource(R.string.search_result_loading_more)
            ManiculeLoading(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ManiculeSize.touchTargetMin)
                        .semantics { contentDescription = description },
            )
        }

        is LoadState.Error -> {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.search_result_append_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                ManiculeTextButton(
                    onClick = onRetry,
                    text = stringResource(R.string.search_retry),
                )
            }
        }

        is LoadState.NotLoading -> Unit
    }
}

@ManiculePreview
@Composable
private fun SearchResultContentPreview() {
    val books =
        flowOf(
            PagingData.from(BookPreviewParameterProvider().values.toList()),
        ).collectAsLazyPagingItems()

    ManiculePreviewTheme {
        SearchResultList(
            searchRequestId = 0L,
            books = books,
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
        EmptySearchResult()
    }
}
