package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode

@Immutable
sealed interface SettingsEvent {
    data class ReminderUpdateFailed(
        val desiredConfig: ReminderConfig,
    ) : SettingsEvent

    data object DismissReminderUpdateFailure : SettingsEvent
}

@Immutable
sealed interface ThemeEvent {
    data class UpdateFailed(
        val desiredMode: ThemeMode,
    ) : ThemeEvent

    data object DismissUpdateFailure : ThemeEvent
}
