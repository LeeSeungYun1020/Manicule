package com.leeseungyun1020.manicule.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasScrollToIndexAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_requestsInitialSearchFocusAndNavigatesBack() {
        var navigatedBack = false
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState = RecentQueriesState.Content(emptyList()),
                ),
            onNavigateBack = { navigatedBack = true },
        )

        composeTestRule.onNodeWithText("What book are you looking for?").assertIsDisplayed()
        composeTestRule
            .onNode(hasText("Search by title, author, or ISBN") and hasSetTextAction())
            .assertIsFocused()
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        assertThat(navigatedBack).isTrue()
    }

    @Test
    fun recentQueries_areDisplayed() {
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState =
                        RecentQueriesState.Content(
                            listOf("Compose", "A long recent search query"),
                        ),
                ),
        )

        composeTestRule.onNodeWithText("Recent searches").assertIsDisplayed()
        composeTestRule.onNodeWithText("Compose").assertIsDisplayed()
        composeTestRule.onNodeWithText("A long recent search query").assertIsDisplayed()
    }

    @Test
    fun loadingState_isAccessible() {
        composeTestRule.setSearchContent(uiState = SearchUiState())

        composeTestRule.onNodeWithContentDescription("Loading recent searches").assertIsDisplayed()
    }

    @Test
    fun unavailableState_displaysEmptyStateAndKeepsSearchFocused() {
        composeTestRule.setSearchContent(
            uiState = SearchUiState(recentQueriesState = RecentQueriesState.Unavailable),
        )

        composeTestRule.onNodeWithText("What book are you looking for?").assertIsDisplayed()
        composeTestRule
            .onNode(hasText("Search by title, author, or ISBN") and hasSetTextAction())
            .assertIsFocused()
    }

    @Test
    fun content_isDisplayedAtCompactWidth() {
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState = RecentQueriesState.Content(listOf("Compact")),
                ),
            width = 320,
        )

        composeTestRule.onNodeWithText("Compact").assertIsDisplayed()
    }

    @Test
    fun content_isDisplayedAtExpandedWidth() {
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState = RecentQueriesState.Content(listOf("Expanded")),
                ),
            width = 840,
        )

        composeTestRule.onNodeWithText("Expanded").assertIsDisplayed()
    }

    @Test
    fun recentQueryClick_selectsQuery() {
        var selectedQuery: String? = null
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState =
                        RecentQueriesState.Content(listOf("Compose")),
                ),
            onQuerySelected = { selectedQuery = it },
        )

        composeTestRule.onNodeWithText("Compose").performClick()

        assertThat(selectedQuery).isEqualTo("Compose")
    }

    @Test
    fun deleteButton_invokesOnDeleteQuery() {
        var deletedQuery: String? = null
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState =
                        RecentQueriesState.Content(listOf("Compose", "Kotlin")),
                ),
            onDeleteQuery = { deletedQuery = it },
        )

        composeTestRule.onNodeWithContentDescription("Delete Compose").performClick()
        assertThat(deletedQuery).isEqualTo("Compose")
    }

    @Test
    fun clearAllButton_invokesOnClearAll() {
        var clearAllInvoked = false
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState =
                        RecentQueriesState.Content(listOf("Compose", "Kotlin")),
                ),
            onClearAll = { clearAllInvoked = true },
        )

        composeTestRule.onNodeWithText("Clear all").performClick()
        assertThat(clearAllInvoked).isTrue()
    }

    @Test
    fun snackbar_displaysQueryDeletedMessageAndUndoButton() {
        var undoInvoked = false
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState =
                        RecentQueriesState.Content(listOf("Kotlin")),
                    snackbarMessage =
                        SearchSnackbarMessage.QueryDeleted(id = 1L, query = "Compose"),
                ),
            onUndoDelete = { undoInvoked = true },
        )

        composeTestRule.onNodeWithText("Search query deleted").assertIsDisplayed()
        composeTestRule.onNodeWithText("Undo").assertIsDisplayed().performClick()
        assertThat(undoInvoked).isTrue()
    }

    @Test
    fun snackbar_displaysAllQueriesDeletedMessage() {
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    recentQueriesState =
                        RecentQueriesState.Content(emptyList()),
                    snackbarMessage =
                        SearchSnackbarMessage.AllQueriesDeleted(id = 1L),
                ),
        )

        composeTestRule.onNodeWithText("All recent searches deleted").assertIsDisplayed()
        composeTestRule.onNodeWithText("Undo").assertIsDisplayed()
    }

    @Test
    fun filteredQueries_doNotShowDeleteButton() {
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    query = "Comp",
                    inputPhase = SearchInputPhase.TYPING,
                    filteredQueries = listOf("Compose"),
                ),
        )

        composeTestRule.onNodeWithText("Compose").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Clear all").assertCountEquals(0)
        composeTestRule.onNodeWithContentDescription("Delete Compose").assertDoesNotExist()
    }

    @Test
    fun typing_displaysOnlyFilteredQueriesAndSelectsMatch() {
        var selectedQuery = ""
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    query = "compose",
                    inputPhase = SearchInputPhase.TYPING,
                    recentQueriesState =
                        RecentQueriesState.Content(
                            listOf("Jetpack Compose", "Kotlin"),
                        ),
                    filteredQueries = listOf("Jetpack Compose"),
                ),
            initialQuery = "compose",
            onQuerySelected = { selectedQuery = it },
        )

        composeTestRule.onNodeWithText("Jetpack Compose").performClick()
        composeTestRule.onAllNodesWithText("Kotlin").assertCountEquals(0)
        assertThat(selectedQuery).isEqualTo("Jetpack Compose")
    }

    @Test
    fun imeAction_submitsInput() {
        var submittedQuery = ""
        composeTestRule.setSearchContent(
            uiState = SearchUiState(),
            onSearch = { submittedQuery = it },
        )

        val searchField = composeTestRule.onNode(hasSetTextAction())
        searchField.performTextInput("Compose")
        searchField.performImeAction()

        assertThat(submittedQuery).isEqualTo("Compose")
    }

    @Test
    fun submittedSearch_displaysPagedBooks() {
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    query = "Compose",
                    inputPhase = SearchInputPhase.SUBMITTED,
                ),
            searchResults =
                flowOf(
                    loadedSearchData(
                        listOf(book(title = "Compose in Action")),
                    ),
                ),
            initialQuery = "Compose",
        )

        composeTestRule.onNodeWithText("Search results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Compose in Action").assertIsDisplayed()
    }

    @Test
    fun repeatedSearch_resetsScrollWhileOtherStateUpdatesKeepPosition() {
        val state =
            mutableStateOf(
                SearchUiState(
                    query = "Compose",
                    inputPhase = SearchInputPhase.SUBMITTED,
                    searchRequestId = 0L,
                ),
            )
        val results =
            flowOf(
                PagingData.from(
                    (1..20).map { book("Book $it") },
                    sourceLoadStates = completedLoadStates,
                ),
            )
        composeTestRule.setContent {
            ManiculeTheme {
                SearchScreen(
                    uiState = state.value,
                    searchResults = results,
                    searchFieldState = rememberTextFieldState(initialText = "Compose"),
                    onSearch = { state.value = state.value.copy(searchRequestId = 1L) },
                    onQuerySelected = {},
                    onNavigateBack = {},
                    onBookSelected = {},
                    scannerAction = SearchScannerAction.Unavailable,
                )
            }
        }

        composeTestRule.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Book 20"))
        composeTestRule.onNodeWithText("Book 20").assertIsDisplayed()
        composeTestRule.runOnIdle {
            state.value = state.value.copy(recentQueriesState = RecentQueriesState.Content(listOf("Compose")))
        }
        composeTestRule.onNodeWithText("Book 20").assertIsDisplayed()

        composeTestRule.onNode(hasSetTextAction()).performImeAction()

        composeTestRule.onNodeWithText("Search results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Book 1").assertIsDisplayed()
    }

    @Test
    fun submittedSearch_withNoBooksDisplaysEmptyResult() {
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    query = "Missing",
                    inputPhase = SearchInputPhase.SUBMITTED,
                ),
            searchResults = flowOf(loadedSearchData()),
            initialQuery = "Missing",
        )

        composeTestRule.onNodeWithText("No search results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Scan the barcode on your book to find it").assertIsDisplayed()
    }

    @Test
    fun resultLoading_isAccessible() {
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    query = "Compose",
                    inputPhase = SearchInputPhase.SUBMITTED,
                ),
            searchResults =
                Pager(PagingConfig(pageSize = 10)) {
                    PendingBookPagingSource()
                }.flow,
        )

        composeTestRule
            .onNodeWithContentDescription("Loading search results")
            .assertIsDisplayed()
    }

    @Test
    fun resultError_displaysRetryAndRetriesLoad() {
        val pagingSource = ErrorBookPagingSource()
        composeTestRule.setSearchContent(
            uiState =
                SearchUiState(
                    query = "Compose",
                    inputPhase = SearchInputPhase.SUBMITTED,
                ),
            searchResults =
                Pager(PagingConfig(pageSize = 10)) {
                    pagingSource
                }.flow,
        )

        composeTestRule.onNodeWithText("Couldn’t load search results").assertIsDisplayed()
        composeTestRule.onNodeWithText("Try again").performClick()
        composeTestRule.onNodeWithText("Recovered book").assertIsDisplayed()
    }
}

private val completedLoadStates =
    LoadStates(
        refresh = LoadState.NotLoading(false),
        prepend = LoadState.NotLoading(true),
        append = LoadState.NotLoading(true),
    )

private fun ComposeContentTestRule.setSearchContent(
    uiState: SearchUiState,
    searchResults: Flow<PagingData<Book>> = flowOf(loadedSearchData()),
    initialQuery: String = "",
    onSearch: (String) -> Unit = {},
    onQuerySelected: (String) -> Unit = {},
    onDeleteQuery: (String) -> Unit = {},
    onClearAll: () -> Unit = {},
    onUndoDelete: () -> Unit = {},
    onSnackbarDismissed: (Long) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    width: Int? = null,
) {
    setContent {
        ManiculeTheme {
            Box(modifier = width?.let { Modifier.width(it.dp) } ?: Modifier) {
                SearchScreen(
                    uiState = uiState,
                    searchResults = searchResults,
                    searchFieldState = rememberTextFieldState(initialText = initialQuery),
                    onSearch = onSearch,
                    onQuerySelected = onQuerySelected,
                    onDeleteQuery = onDeleteQuery,
                    onClearAll = onClearAll,
                    onUndoDelete = onUndoDelete,
                    onSnackbarDismissed = onSnackbarDismissed,
                    onNavigateBack = onNavigateBack,
                    onBookSelected = {},
                    scannerAction = SearchScannerAction.Unavailable,
                )
            }
        }
    }
}

private class PendingBookPagingSource : PagingSource<Int, Book>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> = awaitCancellation()

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? = null
}

private class ErrorBookPagingSource : PagingSource<Int, Book>() {
    @Volatile
    var loadCount = 0
        private set

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        loadCount += 1
        return if (loadCount == 1) {
            LoadResult.Error(IllegalStateException("network unavailable"))
        } else {
            LoadResult.Page(listOf(book("Recovered book")), prevKey = null, nextKey = null)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? = null
}

private fun book(title: String) =
    Book(
        isbn = title,
        title = title,
        author = "Author",
        publisher = "Publisher",
        publishedDate = null,
        coverUrl = null,
        totalPages = null,
        price = null,
        category = null,
        tableOfContentsUrl = null,
        introductionUrl = null,
        summaryUrl = null,
    )
