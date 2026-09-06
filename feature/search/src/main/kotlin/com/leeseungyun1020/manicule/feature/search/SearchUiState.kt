package com.leeseungyun1020.manicule.feature.search

import androidx.compose.runtime.Immutable

@Immutable
data class SearchUiState(
    val query: String = "",
    val inputPhase: SearchInputPhase = SearchInputPhase.IDLE,
    val recentQueriesState: RecentQueriesState = RecentQueriesState.Loading,
    val filteredQueries: List<String> = emptyList(),
)

enum class SearchInputPhase {
    IDLE,
    TYPING,
    SUBMITTED,
}

@Immutable
sealed interface RecentQueriesState {
    data object Loading : RecentQueriesState

    data object Unavailable : RecentQueriesState

    data class Content(
        val recentQueries: List<String>,
    ) : RecentQueriesState
}
