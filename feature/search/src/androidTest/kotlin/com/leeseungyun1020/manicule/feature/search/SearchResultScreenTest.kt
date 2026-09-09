package com.leeseungyun1020.manicule.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class SearchResultScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun rowTap_selectsIsbnFromCoverAndText() {
        val selections = mutableListOf<String>()
        showResults(flowOf(loadedSearchData(listOf(resultBook(0)))), onBookSelected = selections::add)

        compose.onNodeWithContentDescription("Book 0", useUnmergedTree = true)
            .performTouchInput { click(center) }
        compose.onNodeWithText("Book 0", useUnmergedTree = true)
            .performTouchInput { click(center) }

        assertThat(selections).containsExactly("isbn-0", "isbn-0")
    }

    @Test
    fun emptyResult_displaysDisabledScannerAtCompactWidth() {
        showResults(flowOf(loadedSearchData()))
        compose.onNodeWithText("Scan the barcode on your book to find it").assertIsDisplayed()
        compose.onNodeWithText("Scan").assertIsDisplayed().assertIsNotEnabled()
    }

    @Test
    fun emptyResult_availableScannerCallsCallback() {
        var navigations = 0
        showResults(
            flowOf(loadedSearchData()),
            scannerAction = SearchScannerAction.Available { navigations++ },
        )
        compose.onNodeWithText("Scan").performClick()
        assertThat(navigations).isEqualTo(1)
    }

    @Test
    fun appendLoading_keepsRowsAndAddsNextPage() {
        val source = ControlledResultSource()
        showResults(pagedResults(source))
        compose.onNodeWithTag("search_results").performScrollToIndex(30)
        compose.waitUntil { source.appendCalls > 0 }
        compose.onNodeWithText("Book 29").assertIsDisplayed()
        compose.onNodeWithTag("search_results").performScrollToNode(hasContentDescription("Loading more search results"))
        compose.onNodeWithContentDescription("Loading more search results").assertIsDisplayed()

        source.appendGate.complete(Unit)
        compose.waitUntil { source.appendCompleted }
        compose.onNodeWithTag("search_results").performScrollToIndex(40)
        compose.onNodeWithText("Book 39").assertIsDisplayed()
        compose.onNodeWithContentDescription("Loading more search results").assertDoesNotExist()
    }

    @Test
    fun appendFailure_keepsRowsAndRetryRecovers() {
        val source = ControlledResultSource(failAppend = true)
        source.appendGate.complete(Unit)
        showResults(pagedResults(source))
        compose.onNodeWithTag("search_results").performScrollToIndex(30)
        compose.onNodeWithTag("search_results").performScrollToNode(hasText("Couldn’t load more results"))
        compose.onNodeWithText("Couldn’t load more results").assertIsDisplayed()
        compose.onNodeWithText("Book 29").assertIsDisplayed()

        source.failAppend = false
        compose.onNodeWithText("Try again").performClick()
        compose.waitUntil { source.appendCompleted }
        compose.onNodeWithTag("search_results").performScrollToIndex(40)
        compose.onNodeWithText("Book 39").assertIsDisplayed()
        compose.onNodeWithText("Couldn’t load more results").assertDoesNotExist()
        assertThat(source.refreshCalls).isEqualTo(1)
        assertThat(source.appendCalls).isEqualTo(2)
    }

    @Test
    fun restoredScreen_keepsScrollPosition() {
        val restoration = StateRestorationTester(compose)
        val results = flowOf(loadedSearchData((0..39).map(::resultBook)))
        restoration.setContent {
            ManiculeTheme {
                SearchScreen(
                    uiState = SearchUiState(query = "Book", inputPhase = SearchInputPhase.SUBMITTED, searchRequestId = 1),
                    searchResults = results,
                    searchFieldState = rememberTextFieldState("Book"),
                    onSearch = {},
                    onQuerySelected = {},
                    onNavigateBack = {},
                    onBookSelected = {},
                    scannerAction = SearchScannerAction.Unavailable,
                )
            }
        }
        compose.onNodeWithTag("search_results").performScrollToIndex(25)
        compose.onNodeWithText("Book 24").assertIsDisplayed()
        restoration.emulateSavedInstanceStateRestore()
        compose.onNodeWithText("Book 24").assertIsDisplayed()
        compose.onNodeWithText("Book 0").assertDoesNotExist()
    }

    @Test
    fun sameQueryNewSubmission_resetsScroll() {
        val state = mutableStateOf(SearchUiState(query = "Book", inputPhase = SearchInputPhase.SUBMITTED, searchRequestId = 1))
        val results = flowOf(loadedSearchData((0..39).map(::resultBook)))
        compose.setContent {
            ManiculeTheme {
                SearchScreen(
                    uiState = state.value,
                    searchResults = results,
                    searchFieldState = rememberTextFieldState("Book"),
                    onSearch = {},
                    onQuerySelected = {},
                    onNavigateBack = {},
                    onBookSelected = {},
                    scannerAction = SearchScannerAction.Unavailable,
                )
            }
        }
        compose.onNodeWithTag("search_results").performScrollToIndex(25)
        compose.runOnIdle { state.value = state.value.copy(searchRequestId = 2) }
        compose.onNodeWithText("Book 0").assertIsDisplayed()
    }

    private fun showResults(
        results: Flow<PagingData<Book>>,
        onBookSelected: (String) -> Unit = {},
        scannerAction: SearchScannerAction = SearchScannerAction.Unavailable,
    ) {
        compose.setContent {
            ManiculeTheme {
                Box(Modifier.width(320.dp)) {
                    SearchScreen(
                        uiState = SearchUiState(query = "Book", inputPhase = SearchInputPhase.SUBMITTED),
                        searchResults = results,
                        searchFieldState = rememberTextFieldState("Book"),
                        onSearch = {},
                        onQuerySelected = {},
                        onNavigateBack = {},
                        onBookSelected = onBookSelected,
                        scannerAction = scannerAction,
                    )
                }
            }
        }
    }
}

private fun pagedResults(source: ControlledResultSource) =
    Pager(PagingConfig(pageSize = 30, initialLoadSize = 30, prefetchDistance = 1)) { source }.flow

private class ControlledResultSource(
    @Volatile var failAppend: Boolean = false,
) : PagingSource<Int, Book>() {
    val appendGate = CompletableDeferred<Unit>()

    @Volatile var appendCalls = 0

    @Volatile var refreshCalls = 0

    @Volatile var appendCompleted = false

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        if (params.key == null) {
            refreshCalls++
            return LoadResult.Page((0..29).map(::resultBook), prevKey = null, nextKey = 2)
        }
        appendCalls++
        appendGate.await()
        if (failAppend) return LoadResult.Error(IllegalStateException("append unavailable"))
        appendCompleted = true
        return LoadResult.Page((30..39).map(::resultBook), prevKey = 1, nextKey = null)
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? = null
}

private fun resultBook(index: Int) =
    Book(
        isbn = "isbn-$index",
        title = "Book $index",
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

internal fun loadedSearchData(books: List<Book> = emptyList()): PagingData<Book> =
    PagingData.from(
        books,
        sourceLoadStates = androidx.paging.LoadStates(
            refresh = androidx.paging.LoadState.NotLoading(false),
            prepend = androidx.paging.LoadState.NotLoading(true),
            append = androidx.paging.LoadState.NotLoading(true),
        ),
    )
