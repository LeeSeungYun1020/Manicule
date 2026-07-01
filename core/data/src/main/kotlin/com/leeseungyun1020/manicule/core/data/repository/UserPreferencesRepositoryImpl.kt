package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.datasource.UserPreferencesLocalDataSource
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesRepositoryImpl
    @Inject
    constructor(
        private val userPreferencesLocalDataSource: UserPreferencesLocalDataSource,
    ) : UserPreferencesRepository {
        override val userPreferences: Flow<UserPreferences>
            get() = userPreferencesLocalDataSource.userPreferencesFlow

        override suspend fun setThemeMode(themeMode: ThemeMode) {
            userPreferencesLocalDataSource.setThemeMode(themeMode)
        }

        override suspend fun setReminderConfig(config: ReminderConfig) {
            userPreferencesLocalDataSource.setReminderConfig(config)
        }
    }
