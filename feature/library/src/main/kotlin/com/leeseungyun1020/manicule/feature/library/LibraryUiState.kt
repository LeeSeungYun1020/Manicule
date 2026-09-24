package com.leeseungyun1020.manicule.feature.library

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus

@Immutable
sealed interface LibraryUiState {
    val selectedStatus: ReadingStatus
    val sort: LibrarySort

    data class Loading(
        override val selectedStatus: ReadingStatus,
        override val sort: LibrarySort = LibrarySort.Default,
    ) : LibraryUiState

    data class Content(
        override val selectedStatus: ReadingStatus,
        val books: List<BookEntry>,
        override val sort: LibrarySort = LibrarySort.Default,
    ) : LibraryUiState

    data class Error(
        override val selectedStatus: ReadingStatus,
        override val sort: LibrarySort = LibrarySort.Default,
    ) : LibraryUiState
}

enum class LibraryActionMessageKind {
    STATUS_CHANGED,
    DELETED,
    ACTION_FAILED,
    UNDO_FAILED,
}

data class LibraryActionMessage(
    val id: Long,
    val kind: LibraryActionMessageKind,
)
