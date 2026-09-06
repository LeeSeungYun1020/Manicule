package com.leeseungyun1020.manicule.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.leeseungyun1020.manicule.core.domain.search.GetRecentQueriesUseCase
import com.leeseungyun1020.manicule.core.domain.search.SaveRecentQueryUseCase
import com.leeseungyun1020.manicule.core.domain.search.SearchBooksUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.SearchQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RECENT_QUERIES_RETRY_DELAY_MILLIS = 500L

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        getRecentQueries: GetRecentQueriesUseCase,
        private val saveRecentQuery: SaveRecentQueryUseCase,
        private val searchBooks: SearchBooksUseCase,
    ) : ViewModel() {
        private val searchInput = MutableStateFlow(SearchInput())
        private val searchRequest = MutableStateFlow<SearchRequest?>(null)
        private var nextRequestId = 0L

        private val recentQueriesState =
            getRecentQueries()
                .map { queries -> queries.toRecentQueriesState() }
                .retryWhen { cause, attempt ->
                    if (cause is CancellationException || attempt > 0L) {
                        false
                    } else {
                        delay(RECENT_QUERIES_RETRY_DELAY_MILLIS)
                        true
                    }
                }
                .catch { emit(RecentQueriesState.Unavailable) }
                .onStart { emit(RecentQueriesState.Loading) }

        val uiState =
            combine(recentQueriesState, searchInput) { recentState, input ->
                val recentQueries =
                    (recentState as? RecentQueriesState.Content)
                        ?.recentQueries
                        .orEmpty()
                SearchUiState(
                    query = input.query,
                    inputPhase = input.phase,
                    recentQueriesState = recentState,
                    filteredQueries =
                        if (input.phase == SearchInputPhase.TYPING) {
                            recentQueries.filter { query ->
                                query.contains(input.query, ignoreCase = true)
                            }
                        } else {
                            emptyList()
                        },
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = SearchUiState(),
            )

        val searchResults: Flow<PagingData<Book>> =
            searchRequest
                .flatMapLatest { request ->
                    if (request == null) {
                        flowOf(PagingData.empty())
                    } else {
                        searchBooks(request.query)
                    }
                }
                .cachedIn(viewModelScope)

        fun onQueryChanged(query: String) {
            val normalizedQuery = query.trim()
            if (searchInput.value.query == normalizedQuery) return

            searchInput.value =
                SearchInput(
                    query = normalizedQuery,
                    phase =
                        if (normalizedQuery.isEmpty()) {
                            SearchInputPhase.IDLE
                        } else {
                            SearchInputPhase.TYPING
                        },
                )
        }

        fun onSearch(query: String) {
            val normalizedQuery = query.trim()
            if (normalizedQuery.isEmpty()) return

            searchInput.value =
                SearchInput(
                    query = normalizedQuery,
                    phase = SearchInputPhase.SUBMITTED,
                )
            searchRequest.value =
                SearchRequest(
                    id = nextRequestId++,
                    query = normalizedQuery,
                )
            viewModelScope.launch {
                try {
                    saveRecentQuery(normalizedQuery)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    // 검색 기록 저장 실패는 검색 결과를 막지 않는다.
                }
            }
        }
    }

private fun List<SearchQuery>.toRecentQueriesState(): RecentQueriesState =
    RecentQueriesState.Content(
        recentQueries = map { it.query },
    )

private data class SearchInput(
    val query: String = "",
    val phase: SearchInputPhase = SearchInputPhase.IDLE,
)

private data class SearchRequest(
    val id: Long,
    val query: String,
)
