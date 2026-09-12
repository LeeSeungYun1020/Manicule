package com.leeseungyun1020.manicule.feature.search

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.testing.asPagingSourceFactory
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import com.leeseungyun1020.manicule.core.domain.search.GetRecentQueriesUseCase
import com.leeseungyun1020.manicule.core.domain.search.SaveRecentQueryUseCase
import com.leeseungyun1020.manicule.core.domain.search.SearchBooksUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun uiState_emitsLoadingThenRecentQueries() =
        runTest(testDispatcher) {
            val repository = FakeSearchHistoryRepository { flowOf(listOf(searchQuery("Compose"))) }
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState())
                assertThat(awaitItem())
                    .isEqualTo(
                        SearchUiState(
                            recentQueriesState = RecentQueriesState.Content(listOf("Compose")),
                        ),
                    )
                assertThat(repository.observedLimits).containsExactly(10)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun uiState_emitsEmptyContent() =
        runTest(testDispatcher) {
            var collectionCount = 0
            val repository =
                FakeSearchHistoryRepository {
                    flow {
                        collectionCount += 1
                        emit(emptyList())
                    }
                }
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState())
                assertThat(awaitItem())
                    .isEqualTo(
                        SearchUiState(
                            recentQueriesState = RecentQueriesState.Content(emptyList()),
                        ),
                    )
                assertThat(collectionCount).isEqualTo(1)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun uiState_forwardsRepositoryReemissions() =
        runTest(testDispatcher) {
            val queries = MutableSharedFlow<List<SearchQuery>>(replay = 1)
            val repository = FakeSearchHistoryRepository { queries }
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState())
                queries.emit(listOf(searchQuery("Kotlin")))
                assertThat(awaitItem().recentQueriesState)
                    .isEqualTo(RecentQueriesState.Content(listOf("Kotlin")))
                queries.emit(listOf(searchQuery("Compose"), searchQuery("Kotlin")))
                assertThat(awaitItem().recentQueriesState)
                    .isEqualTo(RecentQueriesState.Content(listOf("Compose", "Kotlin")))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun queryChange_filtersRecentQueriesImmediatelyAndClearRestoresIdle() =
        runTest(testDispatcher) {
            val repository =
                FakeSearchHistoryRepository {
                    flowOf(
                        listOf(
                            searchQuery("Jetpack Compose"),
                            searchQuery("Kotlin"),
                        ),
                    )
                }
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                awaitItem()
                awaitItem()

                viewModel.onQueryChanged("COMPOSE")
                assertThat(awaitItem())
                    .isEqualTo(
                        SearchUiState(
                            query = "COMPOSE",
                            inputPhase = SearchInputPhase.TYPING,
                            recentQueriesState =
                                RecentQueriesState.Content(
                                    listOf("Jetpack Compose", "Kotlin"),
                                ),
                            filteredQueries = listOf("Jetpack Compose"),
                        ),
                    )

                viewModel.onQueryChanged("")
                assertThat(awaitItem())
                    .isEqualTo(
                        SearchUiState(
                            recentQueriesState =
                                RecentQueriesState.Content(
                                    listOf("Jetpack Compose", "Kotlin"),
                                ),
                        ),
                    )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun queryChange_doesNotSearchOrSave() =
        runTest(testDispatcher) {
            val historyRepository = FakeSearchHistoryRepository { flowOf(emptyList()) }
            val bookRepository = FakeBookRepository()
            val viewModel = createViewModel(historyRepository, bookRepository)

            viewModel.onQueryChanged("Compose")
            runCurrent()

            assertThat(historyRepository.savedQueries).isEmpty()
            assertThat(bookRepository.searchQueries).isEmpty()
        }

    @Test
    fun search_trimsQuerySavesHistoryAndReturnsBooks() =
        runTest(testDispatcher) {
            val historyRepository = FakeSearchHistoryRepository { flowOf(emptyList()) }
            val bookRepository = FakeBookRepository()
            val viewModel = createViewModel(historyRepository, bookRepository)

            viewModel.onSearch("  Compose  ")
            runCurrent()
            val books = viewModel.searchResults.asSnapshot()

            assertThat(historyRepository.savedQueries).containsExactly("Compose")
            assertThat(bookRepository.searchQueries).containsExactly("Compose")
            assertThat(books.single().title).isEqualTo("Compose")
        }

    @Test
    fun sameQuerySubmission_startsNewSearch() =
        runTest(testDispatcher) {
            val historyRepository = FakeSearchHistoryRepository { flowOf(emptyList()) }
            val bookRepository = FakeBookRepository()
            val viewModel = createViewModel(historyRepository, bookRepository)

            viewModel.searchResults.test {
                awaitItem()
                viewModel.onSearch("Compose")
                awaitItem()
                viewModel.onSearch("Compose")
                awaitItem()

                assertThat(bookRepository.searchQueries)
                    .containsExactly("Compose", "Compose")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun sameQuerySubmission_updatesUiRequestId() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(FakeSearchHistoryRepository { flowOf(emptyList()) })

            viewModel.uiState.test {
                awaitItem()
                awaitItem()
                viewModel.onSearch("Compose")
                val firstSearch = awaitItem()

                viewModel.onSearch("Compose")
                val repeatedSearch = awaitItem()

                assertThat(firstSearch.searchRequestId).isNotNull()
                assertThat(repeatedSearch.searchRequestId).isNotEqualTo(firstSearch.searchRequestId)
                assertThat(repeatedSearch.copy(searchRequestId = firstSearch.searchRequestId))
                    .isEqualTo(firstSearch)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun submissions_replacePreviousResultsIncludingSameQuery() =
        runTest(testDispatcher) {
            val repository = FakeBookRepository()
            val viewModel = createViewModel(FakeSearchHistoryRepository { flowOf(emptyList()) }, repository)
            viewModel.searchResults.test {
                awaitItem()
                viewModel.onSearch("First")
                assertThat(flowOf(awaitItem()).asSnapshot().single().title).isEqualTo("First")
                viewModel.onSearch("Second")
                assertThat(flowOf(awaitItem()).asSnapshot().single().title).isEqualTo("Second")
                repository.resultTitle = "Updated second"
                viewModel.onSearch("Second")
                assertThat(flowOf(awaitItem()).asSnapshot().single().title).isEqualTo("Updated second")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun submissionId_changesOnlyWhenSearchIsSubmitted() =
        runTest(testDispatcher) {
            val viewModel = createViewModel(FakeSearchHistoryRepository { flowOf(emptyList()) })
            viewModel.uiState.test {
                awaitItem()
                awaitItem()
                viewModel.onSearch("Book")
                val firstId = awaitItem().searchRequestId
                assertThat(firstId).isNotNull()
                viewModel.onQueryChanged("Another")
                assertThat(awaitItem().searchRequestId).isEqualTo(firstId)
                viewModel.onSearch("Book")
                assertThat(awaitItem().searchRequestId).isNotEqualTo(firstId)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun scrollingSnapshot_loadsAllPages() =
        runTest(testDispatcher) {
            val repository = FakeBookRepository(bookCount = 40)
            val viewModel = createViewModel(FakeSearchHistoryRepository { flowOf(emptyList()) }, repository)
            viewModel.onSearch("Book")
            val books = viewModel.searchResults.asSnapshot { scrollTo(39) }
            assertThat(books).hasSize(40)
            assertThat(books.last().title).isEqualTo("Book 39")
            assertThat(repository.searchQueries).containsExactly("Book")
        }

    @Test
    fun historySaveFailure_doesNotBlockSearch() =
        runTest(testDispatcher) {
            val historyRepository =
                FakeSearchHistoryRepository { flowOf(emptyList()) }.apply {
                    saveFailure = IllegalStateException("database unavailable")
                }
            val viewModel = createViewModel(historyRepository)

            viewModel.onSearch("Compose")
            runCurrent()

            assertThat(viewModel.searchResults.asSnapshot().single().title)
                .isEqualTo("Compose")
        }

    @Test
    fun uiState_retriesOnceAfter500MillisAndRecovers() =
        runTest(testDispatcher) {
            var collectionCount = 0
            val repository =
                FakeSearchHistoryRepository {
                    flow {
                        collectionCount += 1
                        if (collectionCount == 1) {
                            throw IllegalStateException("database unavailable")
                        }
                        emit(listOf(searchQuery("Compose")))
                    }
                }
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState())
                runCurrent()
                assertThat(collectionCount).isEqualTo(1)

                advanceTimeBy(499)
                runCurrent()
                expectNoEvents()
                assertThat(collectionCount).isEqualTo(1)

                advanceTimeBy(1)
                runCurrent()
                assertThat(awaitItem().recentQueriesState)
                    .isEqualTo(RecentQueriesState.Content(listOf("Compose")))
                assertThat(collectionCount).isEqualTo(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun uiState_emitsUnavailableAfterRetryAlsoFails() =
        runTest(testDispatcher) {
            var collectionCount = 0
            val repository =
                FakeSearchHistoryRepository {
                    flow<List<SearchQuery>> {
                        collectionCount += 1
                        throw IllegalStateException("database unavailable")
                    }
                }
            val viewModel = createViewModel(repository)

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState())
                runCurrent()
                assertThat(collectionCount).isEqualTo(1)

                advanceTimeBy(500)
                runCurrent()
                assertThat(awaitItem().recentQueriesState)
                    .isEqualTo(RecentQueriesState.Unavailable)
                assertThat(collectionCount).isEqualTo(2)
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private fun createViewModel(
    historyRepository: FakeSearchHistoryRepository,
    bookRepository: FakeBookRepository = FakeBookRepository(),
) = SearchViewModel(
    getRecentQueries = GetRecentQueriesUseCase(historyRepository),
    saveRecentQuery = SaveRecentQueryUseCase(historyRepository),
    searchBooks = SearchBooksUseCase(bookRepository),
)

private class FakeSearchHistoryRepository(
    private val flowProvider: () -> Flow<List<SearchQuery>>,
) : SearchHistoryRepository {
    val observedLimits = mutableListOf<Int>()
    val savedQueries = mutableListOf<String>()
    var saveFailure: Exception? = null

    override suspend fun saveQuery(query: String) {
        saveFailure?.let { throw it }
        savedQueries += query
    }

    override fun observeRecentQueries(limit: Int): Flow<List<SearchQuery>> {
        observedLimits += limit
        return flowProvider()
    }

    override suspend fun removeQuery(query: String) = Unit

    override suspend fun clearHistory() = Unit
}

private class FakeBookRepository(
    private val bookCount: Int = 1,
) : BookRepository {
    val searchQueries = mutableListOf<String>()
    var resultTitle: String? = null

    override fun observeBook(isbn: String): Flow<Book?> = flowOf(null)

    override suspend fun syncBook(isbn: String): Result<BookSyncStatus> = Result.failure(NoSuchElementException(isbn))

    override fun searchBooks(query: String): Flow<PagingData<Book>> {
        searchQueries += query
        return Pager(
            config = PagingConfig(pageSize = 10, initialLoadSize = 10),
            pagingSourceFactory = List(bookCount) { index ->
                book(resultTitle ?: if (index == 0) query else "$query $index")
            }.asPagingSourceFactory(),
        ).flow
    }
}

private fun searchQuery(query: String) =
    SearchQuery(
        query = query,
        executedAt = Instant.fromEpochMilliseconds(0),
    )

private fun book(query: String) =
    Book(
        isbn = query,
        title = query,
        author = "",
        publisher = "",
        publishedDate = null,
        coverUrl = null,
        totalPages = null,
        price = null,
        category = null,
        tableOfContentsUrl = null,
        introductionUrl = null,
        summaryUrl = null,
    )
