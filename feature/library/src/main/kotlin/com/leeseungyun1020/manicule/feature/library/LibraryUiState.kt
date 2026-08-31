package com.leeseungyun1020.manicule.feature.library

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus

@Immutable
sealed interface LibraryUiState {
    val selectedStatus: ReadingStatus

    data class Loading(
        override val selectedStatus: ReadingStatus,
    ) : LibraryUiState

    data class Content(
        override val selectedStatus: ReadingStatus,
        val books: List<BookEntry>,
    ) : LibraryUiState

    data class Error(
        override val selectedStatus: ReadingStatus,
    ) : LibraryUiState
}
