package com.leeseungyun1020.manicule.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.domain.settings.GetUserPreferencesUseCase
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import com.leeseungyun1020.manicule.core.domain.settings.SetReminderUseCase
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.model.UserPreferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SettingsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeUserPreferencesRepository()
    private val scheduler = FakeReminderScheduler()

    @Test
    fun initialState_loadsSavedReminder() =
        runTest(mainDispatcherRule.dispatcher) {
            val reminder = ReminderConfig(enabled = true, time = LocalTime(8, 30))
            repository.setReminderConfig(reminder)
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SettingsUiState.Loading)
                assertThat(awaitItem()).isEqualTo(SettingsUiState.Content(reminder))
            }
        }

    @Test
    fun enablingAndDisablingReminder_preservesCurrentTime() =
        runTest(mainDispatcherRule.dispatcher) {
            val time = LocalTime(7, 15)
            repository.setReminderConfig(ReminderConfig(enabled = false, time = time))
            val viewModel = viewModel()

            viewModel.uiState.test {
                awaitItem()
                awaitItem()

                viewModel.setReminderEnabled(true)
                advanceUntilIdle()

                assertThat(repository.currentReminder).isEqualTo(ReminderConfig(enabled = true, time = time))
                assertThat(scheduler.scheduledTimes).containsExactly(time)

                viewModel.setReminderEnabled(false)
                advanceUntilIdle()

                assertThat(repository.currentReminder).isEqualTo(ReminderConfig(enabled = false, time = time))
                assertThat(scheduler.cancelCount).isEqualTo(1)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun changingTime_reschedulesEnabledReminder() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.setReminderConfig(ReminderConfig(enabled = true, time = LocalTime(21, 0)))
            val viewModel = viewModel()

            viewModel.uiState.test {
                awaitItem()
                awaitItem()

                viewModel.setReminderTime(LocalTime(9, 45))
                advanceUntilIdle()

                assertThat(repository.currentReminder).isEqualTo(ReminderConfig(enabled = true, time = LocalTime(9, 45)))
                assertThat(scheduler.scheduledTimes).containsExactly(LocalTime(9, 45))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun updateInProgress_ignoresAdditionalChanges() =
        runTest(mainDispatcherRule.dispatcher) {
            val gate = CompletableDeferred<Unit>()
            scheduler.scheduleGate = gate
            val viewModel = viewModel()

            viewModel.uiState.test {
                awaitItem()
                awaitItem()

                viewModel.setReminderEnabled(true)
                runCurrent()
                assertThat(viewModel.uiState.value)
                    .isEqualTo(SettingsUiState.Content(ReminderConfig(enabled = true, time = LocalTime(21, 0)), isUpdating = true))

                viewModel.setReminderEnabled(false)
                gate.complete(Unit)
                advanceUntilIdle()

                assertThat(scheduler.scheduledTimes).containsExactly(LocalTime(21, 0))
                assertThat(scheduler.cancelCount).isEqualTo(0)
                assertThat(repository.currentReminder.enabled).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun updateFailure_restoresPreviousConfigAndCanRetryDesiredConfig() =
        runTest(mainDispatcherRule.dispatcher) {
            val desired = ReminderConfig(enabled = true, time = LocalTime(10, 20))
            val previous = desired.copy(enabled = false)
            repository.setReminderConfig(previous)
            scheduler.scheduleFailure = IOException("failed")
            val viewModel = viewModel()

            viewModel.uiState.test {
                awaitItem()
                awaitItem()
                viewModel.events.test {
                    viewModel.setReminderEnabled(true)
                    advanceUntilIdle()

                    val failure = awaitItem() as SettingsEvent.ReminderUpdateFailed
                    assertThat(failure.desiredConfig).isEqualTo(desired)
                    assertThat(repository.currentReminder).isEqualTo(previous)

                    viewModel.retryReminderUpdate(failure)
                    advanceUntilIdle()

                    assertThat(repository.currentReminder).isEqualTo(desired)
                    assertThat(scheduler.scheduledTimes).containsExactly(desired.time)
                    cancelAndIgnoreRemainingEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun newerSuccessfulUpdate_invalidatesOldRetry() =
        runTest(mainDispatcherRule.dispatcher) {
            scheduler.scheduleFailure = IOException("failed")
            val viewModel = viewModel()

            viewModel.uiState.test {
                awaitItem()
                awaitItem()
                viewModel.events.test {
                    viewModel.setReminderEnabled(true)
                    advanceUntilIdle()
                    val failure = awaitItem() as SettingsEvent.ReminderUpdateFailed

                    viewModel.setReminderTime(LocalTime(8, 30))
                    advanceUntilIdle()
                    val latest = repository.currentReminder
                    val cancellations = scheduler.cancelCount

                    viewModel.retryReminderUpdate(failure)
                    advanceUntilIdle()

                    assertThat(repository.currentReminder).isEqualTo(latest)
                    assertThat(scheduler.scheduledTimes).isEmpty()
                    assertThat(scheduler.cancelCount).isEqualTo(cancellations)
                    expectNoEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun newerFailureWithSameConfig_onlyAllowsLatestRetry() =
        runTest(mainDispatcherRule.dispatcher) {
            scheduler.scheduleFailure = IOException("first failure")
            val viewModel = viewModel()

            viewModel.uiState.test {
                awaitItem()
                awaitItem()
                viewModel.events.test {
                    viewModel.setReminderEnabled(true)
                    advanceUntilIdle()
                    val oldFailure = awaitItem() as SettingsEvent.ReminderUpdateFailed

                    scheduler.scheduleFailure = IOException("second failure")
                    viewModel.setReminderEnabled(true)
                    advanceUntilIdle()
                    val latestFailure = awaitItem() as SettingsEvent.ReminderUpdateFailed
                    assertThat(oldFailure.desiredConfig).isEqualTo(latestFailure.desiredConfig)

                    viewModel.retryReminderUpdate(oldFailure)
                    advanceUntilIdle()
                    assertThat(repository.currentReminder).isEqualTo(ReminderConfig.Default)
                    assertThat(scheduler.scheduledTimes).isEmpty()

                    viewModel.retryReminderUpdate(latestFailure)
                    advanceUntilIdle()
                    viewModel.retryReminderUpdate(latestFailure)
                    advanceUntilIdle()
                    assertThat(repository.currentReminder).isEqualTo(latestFailure.desiredConfig)
                    assertThat(scheduler.scheduledTimes).containsExactly(latestFailure.desiredConfig.time)
                    expectNoEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun preferencesFailure_retrySubscribesAgain() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.failedSubscriptions = 1
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(SettingsUiState.Loading)
                assertThat(awaitItem()).isEqualTo(SettingsUiState.Error)

                viewModel.retryPreferences()

                assertThat(awaitItem()).isEqualTo(SettingsUiState.Loading)
                assertThat(awaitItem()).isEqualTo(SettingsUiState.Content(ReminderConfig.Default))
                assertThat(repository.subscriptionCount).isEqualTo(2)
            }
        }

    private fun viewModel() =
        SettingsViewModel(
            getUserPreferences = GetUserPreferencesUseCase(repository),
            setReminder = SetReminderUseCase(repository, scheduler),
        )
}

private class FakeUserPreferencesRepository : UserPreferencesRepository {
    private val preferences = MutableStateFlow(UserPreferences.Default)
    var failedSubscriptions = 0
    var subscriptionCount = 0

    val currentReminder: ReminderConfig
        get() = preferences.value.reminder

    override val userPreferences: Flow<UserPreferences>
        get() =
            flow {
                subscriptionCount += 1
                if (failedSubscriptions > 0) {
                    failedSubscriptions -= 1
                    throw IOException("failed")
                }
                emitAll(preferences)
            }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        preferences.value = preferences.value.copy(themeMode = themeMode)
    }

    override suspend fun setReminderConfig(config: ReminderConfig) {
        preferences.value = preferences.value.copy(reminder = config)
    }
}

private class FakeReminderScheduler : ReminderScheduler {
    val scheduledTimes = mutableListOf<LocalTime>()
    var cancelCount = 0
    var scheduleFailure: Exception? = null
    var scheduleGate: CompletableDeferred<Unit>? = null

    override suspend fun schedule(time: LocalTime) {
        scheduleGate?.await()
        scheduleFailure?.let { failure ->
            scheduleFailure = null
            throw failure
        }
        scheduledTimes += time
    }

    override suspend fun scheduleNext(time: LocalTime) {
        scheduledTimes += time
    }

    override suspend fun cancel() {
        cancelCount += 1
    }
}
