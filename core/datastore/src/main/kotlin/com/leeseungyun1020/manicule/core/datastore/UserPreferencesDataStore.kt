package com.leeseungyun1020.manicule.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime
import javax.inject.Inject

class UserPreferencesDataStore
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val userPreferencesFlow: Flow<UserPreferences> =
            dataStore.data
                .map { preferences ->
                    preferences.toUserPreferences()
                }

        suspend fun setThemeMode(themeMode: ThemeMode) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME_MODE] = themeMode.name
            }
        }

        suspend fun setReminderConfig(config: ReminderConfig) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.REMINDER_ENABLED] = config.enabled
                preferences[PreferencesKeys.REMINDER_TIME] = config.time.toString()
            }
        }
    }

private fun Preferences.toUserPreferences(): UserPreferences {
    val themeModeName = this[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
    val themeMode =
        runCatching { ThemeMode.valueOf(themeModeName) }
            .getOrDefault(ThemeMode.SYSTEM)

    val reminderEnabled = this[PreferencesKeys.REMINDER_ENABLED] ?: ReminderConfig.Default.enabled

    val reminderTimeStr = this[PreferencesKeys.REMINDER_TIME] ?: ReminderConfig.Default.time.toString()
    val reminderTime =
        runCatching { LocalTime.parse(reminderTimeStr) }
            .getOrDefault(ReminderConfig.Default.time)

    return UserPreferences(
        themeMode = themeMode,
        reminder =
            ReminderConfig(
                enabled = reminderEnabled,
                time = reminderTime,
            ),
    )
}
