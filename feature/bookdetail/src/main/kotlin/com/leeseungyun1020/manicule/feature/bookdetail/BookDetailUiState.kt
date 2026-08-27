package com.leeseungyun1020.manicule.feature.bookdetail

import com.leeseungyun1020.manicule.core.model.BookDetail

enum class BookDetailTab {
    Information,
    MyRecords,
}

sealed interface BookDetailUiState {
    data object Loading : BookDetailUiState

    data object Error : BookDetailUiState

    data class Content(
        val bookDetail: BookDetail,
        val selectedTab: BookDetailTab,
        val refreshStatus: RefreshStatus = RefreshStatus.Idle,
    ) : BookDetailUiState
}

sealed interface RefreshStatus {
    data object Idle : RefreshStatus

    data object Refreshing : RefreshStatus

    data object Failed : RefreshStatus
}
