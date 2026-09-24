package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.model.ThemeMode

@Immutable
sealed interface ThemeUiState {
    data class Loading(
        val previous: ThemeMode? = null,
    ) : ThemeUiState

    data class Error(
        val previous: ThemeMode? = null,
    ) : ThemeUiState

    data class Content(
        val mode: ThemeMode,
    ) : ThemeUiState
}

internal val ThemeUiState.displayedMode: ThemeMode?
    get() = when (this) {
        is ThemeUiState.Loading -> previous
        is ThemeUiState.Error -> previous
        is ThemeUiState.Content -> mode
    }
