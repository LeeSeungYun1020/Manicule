package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.model.ReminderConfig

@Immutable
sealed interface ReminderUiState {
    data class Loading(
        val previous: ReminderConfig? = null,
    ) : ReminderUiState

    data class Error(
        val previous: ReminderConfig? = null,
    ) : ReminderUiState

    data class Content(
        val reminder: ReminderConfig,
        val isUpdating: Boolean = false,
    ) : ReminderUiState
}

internal val ReminderUiState.displayedReminder: ReminderConfig?
    get() = when (this) {
        is ReminderUiState.Loading -> previous
        is ReminderUiState.Error -> previous
        is ReminderUiState.Content -> reminder
    }
