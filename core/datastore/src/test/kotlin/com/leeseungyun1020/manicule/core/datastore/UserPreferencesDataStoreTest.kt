package com.leeseungyun1020.manicule.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class UserPreferencesDataStoreTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private lateinit var userPreferencesDataStore: UserPreferencesDataStore

    @Before
    fun setup() {
        val testDataStore =
            PreferenceDataStoreFactory.create(
                scope = testScope,
                produceFile = {
                    tmpFolder.root.listFiles()?.firstOrNull()
                        ?: tmpFolder.newFile("user_preferences_test.preferences_pb")
                },
            )
        userPreferencesDataStore = UserPreferencesDataStore(testDataStore)
    }

    @Test
    fun shouldReturnDefaultPreferencesWhenEmpty() =
        testScope.runTest {
            val preferences = userPreferencesDataStore.userPreferencesFlow.first()

            assertThat(preferences.themeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(preferences.reminder).isEqualTo(ReminderConfig.Default)
        }

    @Test
    fun shouldUpdateAndReturnThemeMode() =
        testScope.runTest {
            userPreferencesDataStore.setThemeMode(ThemeMode.DARK)

            val preferences = userPreferencesDataStore.userPreferencesFlow.first()

            assertThat(preferences.themeMode).isEqualTo(ThemeMode.DARK)
            assertThat(preferences.reminder).isEqualTo(ReminderConfig.Default)
        }

    @Test
    fun shouldUpdateAndReturnReminderConfig() =
        testScope.runTest {
            val newReminderConfig = ReminderConfig(enabled = true, time = LocalTime(8, 30))
            userPreferencesDataStore.setReminderConfig(newReminderConfig)

            val preferences = userPreferencesDataStore.userPreferencesFlow.first()

            assertThat(preferences.themeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(preferences.reminder.enabled).isTrue()
            assertThat(preferences.reminder.time).isEqualTo(LocalTime(8, 30))
        }

    @Test
    fun shouldReturnDefaultPreferencesWhenInvalidDataIsStored() =
        testScope.runTest {
            val testDataStore =
                PreferenceDataStoreFactory.create(
                    scope = testScope,
                    produceFile = {
                        tmpFolder.root.listFiles()?.firstOrNull() ?: tmpFolder.newFile(
                            "user_preferences_test.preferences_pb",
                        )
                    },
                )

            testDataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME_MODE] = "AUTO"
                preferences[PreferencesKeys.REMINDER_TIME] = "20260623"
            }

            val sut = UserPreferencesDataStore(testDataStore)
            val preferences = sut.userPreferencesFlow.first()

            assertThat(preferences.themeMode).isEqualTo(ThemeMode.SYSTEM)
            assertThat(preferences.reminder.time).isEqualTo(ReminderConfig.Default.time)
        }
}
