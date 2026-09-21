package com.leeseungyun1020.manicule.feature.search

sealed interface SearchUiEvent {
    data class DeleteQuery(
        val query: String,
    ) : SearchUiEvent

    data object ClearAllQueries : SearchUiEvent

    data object UndoDelete : SearchUiEvent

    data object ConfirmDelete : SearchUiEvent

    data class SnackbarDismissed(
        val messageId: Long,
    ) : SearchUiEvent
}
