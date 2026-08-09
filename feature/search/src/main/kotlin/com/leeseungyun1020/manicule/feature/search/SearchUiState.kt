package com.leeseungyun1020.manicule.feature.search

import androidx.compose.runtime.Immutable

@Immutable
sealed interface SearchUiState {
    data object Loading : SearchUiState

    data class Content(
        val recentQueries: List<String>,
    ) : SearchUiState

    data object Error : SearchUiState
}
