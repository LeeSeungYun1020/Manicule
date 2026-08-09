package com.leeseungyun1020.manicule.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.search.GetRecentQueriesUseCase
import com.leeseungyun1020.manicule.core.model.SearchQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        getRecentQueries: GetRecentQueriesUseCase,
    ) : ViewModel() {
        private val retryRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        val uiState =
            retryRequests
                .onStart { emit(Unit) }
                .flatMapLatest {
                    getRecentQueries()
                        .map { queries -> queries.toUiState() }
                        .onStart { emit(SearchUiState.Loading) }
                        .catch { emit(SearchUiState.Error) }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = SearchUiState.Loading,
                )

        fun retry() {
            retryRequests.tryEmit(Unit)
        }
    }

private fun List<SearchQuery>.toUiState(): SearchUiState =
    SearchUiState.Content(
        recentQueries = map { it.query },
    )
