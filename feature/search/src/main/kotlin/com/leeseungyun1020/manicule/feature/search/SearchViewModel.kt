package com.leeseungyun1020.manicule.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.leeseungyun1020.manicule.core.common.di.ApplicationScope
import com.leeseungyun1020.manicule.core.domain.search.ClearRecentQueriesUseCase
import com.leeseungyun1020.manicule.core.domain.search.DeleteRecentQueryUseCase
import com.leeseungyun1020.manicule.core.domain.search.GetRecentQueriesUseCase
import com.leeseungyun1020.manicule.core.domain.search.SaveRecentQueryUseCase
import com.leeseungyun1020.manicule.core.domain.search.SearchBooksUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.SearchQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val RECENT_QUERIES_RETRY_DELAY_MILLIS = 500L

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        getRecentQueries: GetRecentQueriesUseCase,
        private val saveRecentQuery: SaveRecentQueryUseCase,
        private val deleteRecentQuery: DeleteRecentQueryUseCase,
        private val clearRecentQueries: ClearRecentQueriesUseCase,
        searchBooks: SearchBooksUseCase,
        @ApplicationScope private val applicationScope: CoroutineScope,
    ) : ViewModel() {
        private val searchInput = MutableStateFlow(SearchInput())
        private val searchRequest = MutableStateFlow<SearchRequest?>(null)
        private val deletionState = MutableStateFlow(DeletionState())
        private val snackbarMessage = MutableStateFlow<SearchSnackbarMessage?>(null)
        private var nextRequestId = 0L
        private var nextMessageId = 0L

        private val recentQueriesState =
            getRecentQueries()
                .map { queries ->
                    val querySet = queries.map { it.query }.toSet()
                    deletionState.update { state ->
                        state.copy(
                            inFlight = state.inFlight.intersect(querySet),
                            isClearAllInFlight = if (queries.isEmpty()) false else state.isClearAllInFlight,
                        )
                    }
                    queries.toRecentQueriesState()
                }
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
            combine(
                recentQueriesState,
                searchInput,
                deletionState,
                snackbarMessage,
            ) { recentState, input, deletions, message ->
                val rawQueries =
                    (recentState as? RecentQueriesState.Content)
                        ?.recentQueries
                        .orEmpty()
                val effectiveQueries =
                    if (deletions.isClearAllInFlight || deletions.pending is PendingDelete.All) {
                        emptyList()
                    } else {
                        val pendingQuery = (deletions.pending as? PendingDelete.Single)?.query
                        rawQueries.filterNot { it == pendingQuery || it in deletions.inFlight }
                    }
                val effectiveRecentState =
                    when (recentState) {
                        is RecentQueriesState.Content -> RecentQueriesState.Content(effectiveQueries)
                        else -> recentState
                    }
                SearchUiState(
                    query = input.query,
                    inputPhase = input.phase,
                    searchRequestId = input.searchRequestId,
                    recentQueriesState = effectiveRecentState,
                    filteredQueries =
                        if (input.phase == SearchInputPhase.TYPING) {
                            effectiveQueries.filter { query ->
                                query.contains(input.query, ignoreCase = true)
                            }
                        } else {
                            emptyList()
                        },
                    snackbarMessage = message,
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
                searchInput.value.copy(
                    query = normalizedQuery,
                    phase =
                        if (normalizedQuery.isEmpty()) {
                            SearchInputPhase.IDLE
                        } else {
                            SearchInputPhase.TYPING
                        },
                )
        }

        fun onDeleteQuery(query: String) {
            val normalizedQuery = query.trim()
            if (normalizedQuery.isEmpty()) return

            commitPendingDelete()
            deletionState.update { it.copy(pending = PendingDelete.Single(normalizedQuery)) }
            snackbarMessage.value =
                SearchSnackbarMessage.QueryDeleted(
                    id = ++nextMessageId,
                    query = normalizedQuery,
                )
        }

        fun onClearAllQueries() {
            val currentQueries =
                (uiState.value.recentQueriesState as? RecentQueriesState.Content)
                    ?.recentQueries
                    .orEmpty()
            if (currentQueries.isEmpty()) return

            commitPendingDelete()
            deletionState.update { it.copy(pending = PendingDelete.All(currentQueries)) }
            snackbarMessage.value =
                SearchSnackbarMessage.AllQueriesDeleted(
                    id = ++nextMessageId,
                )
        }

        fun onUndoDelete() {
            deletionState.update { it.copy(pending = null) }
            snackbarMessage.value = null
        }

        fun onConfirmDelete() {
            commitPendingDelete()
            snackbarMessage.value = null
        }

        fun onSnackbarDismissed(messageId: Long) {
            if (snackbarMessage.value?.id == messageId) {
                commitPendingDelete()
                snackbarMessage.value = null
            }
        }

        fun onEvent(event: SearchUiEvent) {
            when (event) {
                is SearchUiEvent.DeleteQuery -> onDeleteQuery(event.query)
                SearchUiEvent.ClearAllQueries -> onClearAllQueries()
                SearchUiEvent.UndoDelete -> onUndoDelete()
                SearchUiEvent.ConfirmDelete -> onConfirmDelete()
                is SearchUiEvent.SnackbarDismissed -> onSnackbarDismissed(event.messageId)
            }
        }

        private fun commitPendingDelete() {
            var pendingToCommit: PendingDelete? = null
            deletionState.update { state ->
                pendingToCommit = state.pending
                when (val pending = state.pending) {
                    null -> state
                    is PendingDelete.Single ->
                        state.copy(
                            pending = null,
                            inFlight = state.inFlight + pending.query,
                        )
                    is PendingDelete.All ->
                        state.copy(
                            pending = null,
                            isClearAllInFlight = true,
                        )
                }
            }
            val pending = pendingToCommit ?: return
            applicationScope.launch {
                try {
                    when (pending) {
                        is PendingDelete.Single -> deleteRecentQuery(pending.query)
                        is PendingDelete.All -> clearRecentQueries()
                    }
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    // 검색 기록 삭제 실패는 검색 화면을 차단하지 않는다.
                }
            }
        }

        fun onSearch(query: String) {
            val normalizedQuery = query.trim()
            if (normalizedQuery.isEmpty()) return

            commitPendingDelete()
            deletionState.update { state ->
                state.copy(
                    inFlight = state.inFlight - normalizedQuery,
                    isClearAllInFlight = false,
                )
            }

            val requestId = nextRequestId++

            searchInput.value =
                SearchInput(
                    query = normalizedQuery,
                    phase = SearchInputPhase.SUBMITTED,
                    searchRequestId = requestId,
                )
            searchRequest.value =
                SearchRequest(
                    id = requestId,
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

        override fun onCleared() {
            commitPendingDelete()
            super.onCleared()
        }
    }

private data class DeletionState(
    val pending: PendingDelete? = null,
    val inFlight: Set<String> = emptySet(),
    val isClearAllInFlight: Boolean = false,
)

private sealed interface PendingDelete {
    data class Single(
        val query: String,
    ) : PendingDelete

    data class All(
        val queries: List<String>,
    ) : PendingDelete
}

private fun List<SearchQuery>.toRecentQueriesState(): RecentQueriesState =
    RecentQueriesState.Content(
        recentQueries = map { it.query },
    )

private data class SearchInput(
    val query: String = "",
    val phase: SearchInputPhase = SearchInputPhase.IDLE,
    val searchRequestId: Long? = null,
)

private data class SearchRequest(
    val id: Long,
    val query: String,
)
