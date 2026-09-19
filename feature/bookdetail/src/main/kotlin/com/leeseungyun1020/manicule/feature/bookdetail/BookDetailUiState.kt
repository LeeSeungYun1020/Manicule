package com.leeseungyun1020.manicule.feature.bookdetail

import com.leeseungyun1020.manicule.core.model.BookDetail
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingStatus

enum class BookDetailTab {
    Information,
    MyRecords,
}

sealed interface BookDetailUiState {
    data object Loading : BookDetailUiState

    data object Error : BookDetailUiState

    data class Content(
        val bookDetail: BookDetail,
        val records: List<ReadingRecord> = emptyList(),
        val selectedTab: BookDetailTab,
        val refreshStatus: RefreshStatus = RefreshStatus.Idle,
        val statusChange: StatusChangeState = StatusChangeState.Idle,
        val recordSaving: RecordSavingState = RecordSavingState.Idle,
    ) : BookDetailUiState
}

sealed interface StatusChangeState {
    data object Idle : StatusChangeState

    data class Saving(
        val target: ReadingStatus,
    ) : StatusChangeState

    data class Failed(
        val target: ReadingStatus,
        val attempt: Long,
    ) : StatusChangeState
}

sealed interface RecordSavingState {
    data object Idle : RecordSavingState

    data object Saving : RecordSavingState

    data class Failed(
        val attempt: Long,
    ) : RecordSavingState
}

sealed interface RefreshStatus {
    data object Idle : RefreshStatus

    data object Refreshing : RefreshStatus

    data object Failed : RefreshStatus
}
