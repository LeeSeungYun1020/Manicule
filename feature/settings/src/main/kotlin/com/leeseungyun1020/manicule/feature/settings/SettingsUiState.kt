package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.runtime.Immutable

@Immutable
data class SettingsUiState(
    val reminder: ReminderUiState = ReminderUiState.Loading(),
)
