package com.leeseungyun1020.manicule.feature.search

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.SearchHistoryRepository
import com.leeseungyun1020.manicule.core.domain.search.GetRecentQueriesUseCase
import com.leeseungyun1020.manicule.core.model.SearchQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
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
            val viewModel = SearchViewModel(GetRecentQueriesUseCase(repository))

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState.Loading)
                assertThat(awaitItem()).isEqualTo(SearchUiState.Content(listOf("Compose")))
                assertThat(repository.observedLimits).containsExactly(10)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun uiState_emitsEmptyContent() =
        runTest(testDispatcher) {
            val repository = FakeSearchHistoryRepository { flowOf(emptyList()) }
            val viewModel = SearchViewModel(GetRecentQueriesUseCase(repository))

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState.Loading)
                assertThat(awaitItem()).isEqualTo(SearchUiState.Content(emptyList()))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun uiState_forwardsRepositoryReemissions() =
        runTest(testDispatcher) {
            val queries = MutableSharedFlow<List<SearchQuery>>(replay = 1)
            val repository = FakeSearchHistoryRepository { queries }
            val viewModel = SearchViewModel(GetRecentQueriesUseCase(repository))

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState.Loading)
                queries.emit(listOf(searchQuery("Kotlin")))
                assertThat(awaitItem()).isEqualTo(SearchUiState.Content(listOf("Kotlin")))
                queries.emit(listOf(searchQuery("Compose"), searchQuery("Kotlin")))
                assertThat(awaitItem()).isEqualTo(SearchUiState.Content(listOf("Compose", "Kotlin")))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun uiState_fallsBackToEmptyContentWhenRecentQueryLoadFails() =
        runTest(testDispatcher) {
            val repository =
                FakeSearchHistoryRepository {
                    flow { throw IllegalStateException("database unavailable") }
                }
            val viewModel = SearchViewModel(GetRecentQueriesUseCase(repository))

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SearchUiState.Loading)
                assertThat(awaitItem()).isEqualTo(SearchUiState.Content(emptyList()))
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private class FakeSearchHistoryRepository(
    private val flowProvider: () -> Flow<List<SearchQuery>>,
) : SearchHistoryRepository {
    val observedLimits = mutableListOf<Int>()

    override suspend fun saveQuery(query: String) = Unit

    override fun observeRecentQueries(limit: Int): Flow<List<SearchQuery>> {
        observedLimits += limit
        return flowProvider()
    }

    override suspend fun removeQuery(query: String) = Unit

    override suspend fun clearHistory() = Unit
}

private fun searchQuery(query: String) =
    SearchQuery(
        query = query,
        executedAt = Instant.fromEpochMilliseconds(0),
    )
