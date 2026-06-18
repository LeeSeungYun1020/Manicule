package com.leeseungyun1020.manicule.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime
import java.io.IOException
import javax.inject.Inject

class UserPreferencesDataStore
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val userPreferencesFlow: Flow<UserPreferences> =
            dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }.map { preferences ->
                    val themeModeName = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
                    val themeMode =
                        try {
                            ThemeMode.valueOf(themeModeName)
                        } catch (ignored: IllegalArgumentException) {
                            ThemeMode.SYSTEM
                        }

                    val reminderEnabled = preferences[PreferencesKeys.REMINDER_ENABLED] ?: ReminderConfig.Default.enabled

                    val reminderTimeStr = preferences[PreferencesKeys.REMINDER_TIME] ?: ReminderConfig.Default.time.toString()
                    val reminderTime =
                        try {
                            LocalTime.parse(reminderTimeStr)
                        } catch (ignored: IllegalArgumentException) {
                            ReminderConfig.Default.time
                        }

                    UserPreferences(
                        themeMode = themeMode,
                        reminder =
                            ReminderConfig(
                                enabled = reminderEnabled,
                                time = reminderTime,
                            ),
                    )
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
