package com.leeseungyun1020.manicule.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSearchBar
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSnackbarHost
import com.leeseungyun1020.manicule.core.designsystem.component.showUndoSnackbar
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.feature.search.components.EmptyRecentQuery
import com.leeseungyun1020.manicule.feature.search.components.FilteredQueryList
import com.leeseungyun1020.manicule.feature.search.components.RecentQueryList
import com.leeseungyun1020.manicule.feature.search.components.SearchResultList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

@Composable
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
    onDeleteQuery: (String) -> Unit = {},
    onClearAll: () -> Unit = {},
    onUndoDelete: () -> Unit = {},
    onSnackbarDismissed: (Long) -> Unit = {},
) {
    val books = searchResults.collectAsLazyPagingItems()
    val listState =
        rememberSaveable(uiState.searchRequestId, saver = LazyListState.Saver) {
            LazyListState()
        }
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val message = uiState.snackbarMessage
    LaunchedEffect(message?.id) {
        if (message == null) return@LaunchedEffect
        val messageText =
            when (message) {
                is SearchSnackbarMessage.QueryDeleted ->
                    context.getString(R.string.search_query_deleted)
                is SearchSnackbarMessage.AllQueriesDeleted ->
                    context.getString(R.string.search_all_queries_deleted)
            }
        val undoLabel = context.getString(R.string.search_undo)

        val result =
            snackbarHostState.showUndoSnackbar(
                message = messageText,
                undoLabel = undoLabel,
            )
        if (result == SnackbarResult.ActionPerformed) {
            onUndoDelete()
        } else {
            onSnackbarDismissed(message.id)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { ManiculeSnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
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
                        onDeleteQuery = onDeleteQuery,
                        onClearAll = onClearAll,
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
}

@Composable
private fun IdleSearchContent(
    recentQueriesState: RecentQueriesState,
    onQuerySelected: (String) -> Unit,
    onDeleteQuery: (String) -> Unit,
    onClearAll: () -> Unit,
) {
    when (recentQueriesState) {
        RecentQueriesState.Loading -> SearchLoading()
        RecentQueriesState.Unavailable -> EmptyRecentQuery()
        is RecentQueriesState.Content ->
            if (recentQueriesState.recentQueries.isEmpty()) {
                EmptyRecentQuery()
            } else {
                RecentQueryList(
                    queries = recentQueriesState.recentQueries,
                    onQuerySelected = onQuerySelected,
                    onDeleteQuery = onDeleteQuery,
                    onClearAll = onClearAll,
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
