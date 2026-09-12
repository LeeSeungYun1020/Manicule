package com.leeseungyun1020.manicule.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSearchBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.feature.search.components.FilteredQueryList
import com.leeseungyun1020.manicule.feature.search.components.RecentQueryList
import com.leeseungyun1020.manicule.feature.search.components.SearchResultList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
@Suppress("LongParameterList")
fun SearchScreen(
    uiState: SearchUiState,
    searchResults: Flow<PagingData<Book>>,
    searchFieldState: TextFieldState,
    onSearch: (String) -> Unit,
    onQuerySelected: (String) -> Unit,
    onNavigateBack: () -> Unit,
    onBookSelected: (String) -> Unit,
    scannerAction: SearchScannerAction,
    modifier: Modifier = Modifier,
) {
    val books = searchResults.collectAsLazyPagingItems()
    val listState = rememberSaveable(uiState.searchRequestId, saver = LazyListState.Saver) {
        LazyListState()
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
                .padding(top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        ManiculeSearchBar(
            state = searchFieldState,
            onSearch = onSearch,
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.search_hint),
            requestInitialFocus = uiState.inputPhase != SearchInputPhase.SUBMITTED,
            leadingIcon = {
                ManiculeIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.search_back),
                    )
                }
            },
        )

        when (uiState.inputPhase) {
            SearchInputPhase.IDLE ->
                IdleSearchContent(
                    recentQueriesState = uiState.recentQueriesState,
                    onQuerySelected = onQuerySelected,
                )

            SearchInputPhase.TYPING ->
                if (uiState.filteredQueries.isNotEmpty()) {
                    FilteredQueryList(
                        queries = uiState.filteredQueries,
                        query = uiState.query,
                        onQuerySelected = onQuerySelected,
                    )
                }

            SearchInputPhase.SUBMITTED ->
                SearchResultList(
                    books = books,
                    listState = listState,
                    onBookSelected = onBookSelected,
                    scannerAction = scannerAction,
                )
        }
    }
}

@Composable
private fun IdleSearchContent(
    recentQueriesState: RecentQueriesState,
    onQuerySelected: (String) -> Unit,
) {
    when (recentQueriesState) {
        RecentQueriesState.Loading -> SearchLoading()
        RecentQueriesState.Unavailable -> SearchEmpty()
        is RecentQueriesState.Content ->
            if (recentQueriesState.recentQueries.isEmpty()) {
                SearchEmpty()
            } else {
                RecentQueryList(
                    queries = recentQueriesState.recentQueries,
                    onQuerySelected = onQuerySelected,
                )
            }
    }
}

@Composable
private fun SearchLoading() {
    val description = stringResource(R.string.search_loading)
    ManiculeLoading(
        modifier =
            Modifier
                .fillMaxSize()
                .semantics { contentDescription = description },
    )
}

@Composable
private fun SearchEmpty() {
    ManiculeEmptyState(
        title = stringResource(R.string.search_empty_title),
        description = stringResource(R.string.search_empty_description),
        modifier = Modifier.fillMaxSize(),
        icon = {
            Icon(
                imageVector = ManiculeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(MaterialTheme.size.iconEmptyState),
            )
        },
    )
}

@ManiculePreview
@Preview(name = "Foldable", widthDp = 673, heightDp = 900, showBackground = true)
@Preview(name = "Tablet", widthDp = 1200, heightDp = 900, showBackground = true)
@Composable
private fun EmptySearchScreenPreview() {
    ManiculePreviewTheme {
        SearchScreen(
            uiState =
                SearchUiState(
                    recentQueriesState = RecentQueriesState.Content(emptyList()),
                ),
            searchResults = flowOf(PagingData.empty()),
            searchFieldState = rememberTextFieldState(),
            onSearch = {},
            onQuerySelected = {},
            onNavigateBack = {},
            onBookSelected = {},
            scannerAction = SearchScannerAction.Unavailable,
        )
    }
}

@ManiculePreview
@Composable
private fun UnavailableSearchScreenPreview() {
    ManiculePreviewTheme {
        SearchScreen(
            uiState = SearchUiState(recentQueriesState = RecentQueriesState.Unavailable),
            searchResults = flowOf(PagingData.empty()),
            searchFieldState = rememberTextFieldState(),
            onSearch = {},
            onQuerySelected = {},
            onNavigateBack = {},
            onBookSelected = {},
            scannerAction = SearchScannerAction.Unavailable,
        )
    }
}

@ManiculePreview
@Composable
private fun RecentSearchScreenPreview() {
    ManiculePreviewTheme {
        SearchScreen(
            uiState =
                SearchUiState(
                    recentQueriesState =
                        RecentQueriesState.Content(
                            listOf(
                                "Jetpack Compose",
                                "A very long search query that wraps onto another line for accessibility",
                                "Kotlin coroutines",
                            ),
                        ),
                ),
            searchResults = flowOf(PagingData.empty()),
            searchFieldState = rememberTextFieldState(),
            onSearch = {},
            onQuerySelected = {},
            onNavigateBack = {},
            onBookSelected = {},
            scannerAction = SearchScannerAction.Unavailable,
        )
    }
}

@ManiculePreview
@Composable
private fun LoadingSearchScreenPreview() {
    ManiculePreviewTheme {
        SearchScreen(
            uiState = SearchUiState(),
            searchResults = flowOf(PagingData.empty()),
            searchFieldState = rememberTextFieldState(),
            onSearch = {},
            onQuerySelected = {},
            onNavigateBack = {},
            onBookSelected = {},
            scannerAction = SearchScannerAction.Unavailable,
        )
    }
}
