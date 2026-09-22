package com.leeseungyun1020.manicule.feature.home

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.domain.home.HomeData

@Immutable
sealed interface HomeUiState {
    data object Loading : HomeUiState

    data object Error : HomeUiState

    data class Content(
        val data: HomeData,
    ) : HomeUiState {
        val isFirstUser: Boolean
            get() = !data.hasLibraryBooks && !data.hasReadingRecords
    }
}
