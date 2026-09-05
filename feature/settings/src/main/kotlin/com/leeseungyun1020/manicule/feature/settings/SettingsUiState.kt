package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.model.ReminderConfig

@Immutable
sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data object Error : SettingsUiState

    data class Content(
        val reminder: ReminderConfig,
        val isUpdating: Boolean = false,
    ) : SettingsUiState
}
