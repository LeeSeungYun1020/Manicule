package com.leeseungyun1020.manicule.feature.bookdetail

import com.leeseungyun1020.manicule.core.model.Book

enum class BookDetailTab {
    Information,
    MyRecords,
}

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = true,
    val isFatalError: Boolean = false,
    val isRefreshError: Boolean = false,
    val selectedTab: BookDetailTab = BookDetailTab.Information,
)
