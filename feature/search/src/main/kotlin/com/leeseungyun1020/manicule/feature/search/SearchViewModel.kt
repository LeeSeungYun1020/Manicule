package com.leeseungyun1020.manicule.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.search.GetRecentQueriesUseCase
import com.leeseungyun1020.manicule.core.model.SearchQuery
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

private const val RECENT_QUERIES_RETRY_DELAY_MILLIS = 500L

@HiltViewModel
class SearchViewModel
    @Inject
    constructor(
        getRecentQueries: GetRecentQueriesUseCase,
    ) : ViewModel() {
        val uiState =
            getRecentQueries()
                .map { queries -> queries.toUiState() }
                .retryWhen { cause, attempt ->
                    if (cause is CancellationException || attempt > 0L) {
                        false
                    } else {
                        delay(RECENT_QUERIES_RETRY_DELAY_MILLIS)
                        true
                    }
                }
                .catch { emit(SearchUiState.Unavailable) }
                .onStart { emit(SearchUiState.Loading) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                    initialValue = SearchUiState.Loading,
                )
    }

private fun List<SearchQuery>.toUiState(): SearchUiState =
    SearchUiState.Content(
        recentQueries = map { it.query },
    )
