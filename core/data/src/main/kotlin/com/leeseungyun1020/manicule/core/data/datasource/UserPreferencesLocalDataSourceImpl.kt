package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.datastore.UserPreferencesDataStore
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UserPreferencesLocalDataSourceImpl
    @Inject
    constructor(
        private val userPreferencesDataStore: UserPreferencesDataStore,
    ) : UserPreferencesLocalDataSource {
        override val userPreferencesFlow: Flow<UserPreferences> = userPreferencesDataStore.userPreferencesFlow

        override suspend fun setThemeMode(themeMode: ThemeMode) = userPreferencesDataStore.setThemeMode(themeMode)

        override suspend fun setReminderConfig(config: ReminderConfig) = userPreferencesDataStore.setReminderConfig(config)
    }
