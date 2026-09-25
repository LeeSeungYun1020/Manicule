package com.leeseungyun1020.manicule.feature.settings

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.domain.settings.GetUserPreferencesUseCase
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import com.leeseungyun1020.manicule.core.domain.settings.SetReminderUseCase
import com.leeseungyun1020.manicule.core.domain.settings.SetThemeUseCase
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.model.UserPreferences
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
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
                assertThat(awaitItem().reminder).isEqualTo(ReminderUiState.Loading())
                assertThat(awaitItem().reminder).isEqualTo(ReminderUiState.Content(reminder))
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
                assertThat(viewModel.uiState.value.reminder)
                    .isEqualTo(
                        ReminderUiState.Content(ReminderConfig(enabled = true, time = LocalTime(21, 0)), isUpdating = true),
                    )

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
                    assertThat(awaitItem()).isEqualTo(SettingsEvent.DismissReminderUpdateFailure)
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
                    assertThat(awaitItem()).isEqualTo(SettingsEvent.DismissReminderUpdateFailure)
                    advanceUntilIdle()
                    val latestFailure = awaitItem() as SettingsEvent.ReminderUpdateFailed
                    assertThat(oldFailure.desiredConfig).isEqualTo(latestFailure.desiredConfig)

                    viewModel.retryReminderUpdate(oldFailure)
                    advanceUntilIdle()
                    assertThat(repository.currentReminder).isEqualTo(ReminderConfig.Default)
                    assertThat(scheduler.scheduledTimes).isEmpty()

                    viewModel.retryReminderUpdate(latestFailure)
                    assertThat(awaitItem()).isEqualTo(SettingsEvent.DismissReminderUpdateFailure)
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
    fun updateFailure_replaysEventForRecreatedCollectorUntilRetried() =
        runTest(mainDispatcherRule.dispatcher) {
            val desired = ReminderConfig(enabled = true, time = LocalTime(10, 20))
            scheduler.scheduleFailure = IOException("failed")
            val viewModel = viewModel()

            viewModel.uiState.test {
                awaitItem()
                awaitItem()

                var failureEvent: SettingsEvent.ReminderUpdateFailed? = null
                viewModel.events.test {
                    viewModel.setReminderEnabled(true)
                    advanceUntilIdle()
                    failureEvent = awaitItem() as SettingsEvent.ReminderUpdateFailed
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.events.test {
                    assertThat(awaitItem()).isEqualTo(failureEvent)

                    viewModel.retryReminderUpdate(failureEvent!!)
                    assertThat(awaitItem()).isEqualTo(SettingsEvent.DismissReminderUpdateFailure)
                    advanceUntilIdle()
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.events.test {
                    expectNoEvents()
                }
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun updateFailure_dismissalClearsReplayCache() =
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

                    viewModel.dismissReminderUpdateFailure(failure)
                    cancelAndIgnoreRemainingEvents()
                }

                viewModel.events.test {
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
                assertThat(awaitItem().reminder).isEqualTo(ReminderUiState.Loading())
                assertThat(awaitItem().reminder).isEqualTo(ReminderUiState.Error())

                viewModel.retryPreferences()

                assertThat(awaitItem().reminder).isEqualTo(ReminderUiState.Loading())
                assertThat(awaitItem().reminder).isEqualTo(ReminderUiState.Content(ReminderConfig.Default))
                assertThat(repository.subscriptionCount).isEqualTo(2)
            }
        }

    @Test
    fun initialLoading_blocksChangesAndDuplicateRetries() =
        runTest(mainDispatcherRule.dispatcher) {
            val gate = CompletableDeferred<Unit>()
            repository.readGate = gate
            val viewModel = viewModel()
            runCurrent()

            repeat(3) {
                viewModel.retryPreferences()
                viewModel.setReminderEnabled(true)
                viewModel.setReminderTime(LocalTime(7, 0))
            }
            runCurrent()

            assertThat(viewModel.uiState.value.reminder).isEqualTo(ReminderUiState.Loading())
            assertThat(repository.subscriptionCount).isEqualTo(1)
            assertThat(repository.updates).isEqualTo(0)
            assertThat(scheduler.scheduledTimes).isEmpty()
            assertThat(scheduler.cancelCount).isEqualTo(0)
            gate.complete(Unit)
            runCurrent()
        }

    @Test
    fun readFailure_preservesPreviousValueAndRetryRestoresContent() =
        runTest(mainDispatcherRule.dispatcher) {
            val previous = ReminderConfig(true, LocalTime(8, 30))
            repository.setReminderConfig(previous)
            val viewModel = viewModel()
            runCurrent()
            repository.readFailure.value = true
            runCurrent()
            assertThat(viewModel.uiState.value.reminder).isEqualTo(ReminderUiState.Error(previous))

            val updates = repository.updates
            viewModel.setReminderEnabled(false)
            viewModel.setReminderTime(LocalTime(7, 0))
            runCurrent()
            assertThat(repository.updates).isEqualTo(updates)
            assertThat(scheduler.scheduledTimes).isEmpty()
            assertThat(scheduler.cancelCount).isEqualTo(0)

            repository.readFailure.value = false
            val gate = CompletableDeferred<Unit>()
            repository.readGate = gate
            repeat(3) { viewModel.retryPreferences() }
            runCurrent()
            assertThat(viewModel.uiState.value.reminder).isEqualTo(ReminderUiState.Loading(previous))
            repeat(3) { viewModel.retryPreferences() }
            runCurrent()
            assertThat(repository.subscriptionCount).isEqualTo(2)
            val latest = previous.copy(time = LocalTime(9, 0))
            repository.setReminderConfig(latest)
            gate.complete(Unit)
            runCurrent()
            assertThat(viewModel.uiState.value.reminder).isEqualTo(ReminderUiState.Content(latest))
        }

    @Test
    fun readFailure_invalidatesPendingUpdateRetryEvenAfterRecovery() =
        runTest(mainDispatcherRule.dispatcher) {
            scheduler.scheduleFailure = IOException("schedule failed")
            val viewModel = viewModel()
            runCurrent()
            viewModel.events.test {
                viewModel.setReminderEnabled(true)
                runCurrent()
                val failure = awaitItem() as SettingsEvent.ReminderUpdateFailed

                repository.readFailure.value = true
                runCurrent()
                assertThat(awaitItem()).isEqualTo(SettingsEvent.DismissReminderUpdateFailure)
                assertThat(viewModel.events.replayCache).isEmpty()
                repository.readFailure.value = false
                viewModel.retryPreferences()
                runCurrent()
                val updates = repository.updates
                viewModel.retryReminderUpdate(failure)
                runCurrent()

                assertThat(repository.updates).isEqualTo(updates)
                assertThat(scheduler.scheduledTimes).isEmpty()
                expectNoEvents()
            }
        }

    @Test
    fun readFailureDuringSave_doesNotPublishLateRetry() =
        runTest(mainDispatcherRule.dispatcher) {
            val gate = CompletableDeferred<Unit>()
            scheduler.scheduleGate = gate
            scheduler.scheduleFailure = IOException("schedule failed")
            val viewModel = viewModel()
            runCurrent()
            viewModel.setReminderEnabled(true)
            runCurrent()
            repository.readFailure.value = true
            runCurrent()
            assertThat(viewModel.uiState.value.reminder).isInstanceOf(ReminderUiState.Error::class.java)

            repository.readFailure.value = false
            viewModel.retryPreferences()
            runCurrent()
            gate.complete(Unit)
            runCurrent()

            assertThat(viewModel.events.replayCache).isEmpty()
            assertThat(viewModel.uiState.value.reminder).isEqualTo(ReminderUiState.Content(ReminderConfig.Default))
        }

    @Test
    fun themeSelection_savesAndObservesAllModes() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = viewModel()
            runCurrent()
            assertThat(viewModel.uiState.value.theme).isEqualTo(ThemeUiState.Content(ThemeMode.SYSTEM))
            listOf(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).forEach { mode ->
                viewModel.setThemeMode(mode)
                runCurrent()
                assertThat(repository.currentTheme).isEqualTo(mode)
                assertThat(viewModel.uiState.value.theme).isEqualTo(ThemeUiState.Content(mode))
            }
        }

    @Test
    fun rapidThemeSelection_finishesWithLastMode() =
        runTest(mainDispatcherRule.dispatcher) {
            val gate = CompletableDeferred<Unit>()
            repository.themeWriteGate = gate
            val viewModel = viewModel()
            runCurrent()
            viewModel.setThemeMode(ThemeMode.LIGHT)
            runCurrent()
            viewModel.setThemeMode(ThemeMode.DARK)
            viewModel.setThemeMode(ThemeMode.SYSTEM)
            gate.complete(Unit)
            runCurrent()
            assertThat(repository.themeWrites).containsExactly(ThemeMode.LIGHT, ThemeMode.DARK, ThemeMode.SYSTEM).inOrder()
            assertThat(repository.currentTheme).isEqualTo(ThemeMode.SYSTEM)
        }

    @Test
    fun themeWriteFailure_keepsConfirmedValueAndRetries() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.themeWriteFailure = IOException("write failed")
            val viewModel = viewModel()
            runCurrent()
            viewModel.themeEvents.test {
                viewModel.setThemeMode(ThemeMode.DARK)
                runCurrent()
                val failure = awaitItem() as ThemeEvent.UpdateFailed
                assertThat(repository.currentTheme).isEqualTo(ThemeMode.SYSTEM)
                assertThat(viewModel.uiState.value.theme).isEqualTo(ThemeUiState.Content(ThemeMode.SYSTEM))
                viewModel.resolveThemeUpdateFailure(failure, retry = true)
                runCurrent()
                assertThat(repository.currentTheme).isEqualTo(ThemeMode.DARK)
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun themeReadFailure_keepsPreviousAndRecovers() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = viewModel()
            runCurrent()
            repository.readFailure.value = true
            runCurrent()
            assertThat(viewModel.uiState.value.theme).isEqualTo(ThemeUiState.Error(ThemeMode.SYSTEM))
            viewModel.setThemeMode(ThemeMode.DARK)
            runCurrent()
            assertThat(repository.themeWrites).isEmpty()
            repository.readFailure.value = false
            viewModel.retryPreferences()
            runCurrent()
            assertThat(viewModel.uiState.value.theme).isEqualTo(ThemeUiState.Content(ThemeMode.SYSTEM))
        }

    private fun viewModel() =
        SettingsViewModel(
            getUserPreferences = GetUserPreferencesUseCase(repository),
            setReminder = SetReminderUseCase(repository, scheduler),
            setTheme = SetThemeUseCase(repository),
        )
}

private class FakeUserPreferencesRepository : UserPreferencesRepository {
    private val preferences = MutableStateFlow(UserPreferences.Default)
    var failedSubscriptions = 0
    var subscriptionCount = 0
    var updates = 0
    var themeWriteGate: CompletableDeferred<Unit>? = null
    var themeWriteFailure: Exception? = null
    val themeWrites = mutableListOf<ThemeMode>()
    var readGate: CompletableDeferred<Unit>? = null
    val readFailure = MutableStateFlow(false)

    val currentReminder: ReminderConfig
        get() = preferences.value.reminder

    val currentTheme: ThemeMode
        get() = preferences.value.themeMode

    override val userPreferences: Flow<UserPreferences>
        get() =
            flow {
                subscriptionCount += 1
                readGate?.await()
                if (failedSubscriptions > 0) {
                    failedSubscriptions -= 1
                    throw IOException("failed")
                }
                emitAll(
                    combine(preferences, readFailure) { value, failed ->
                        if (failed) throw IOException("read failed")
                        value
                    },
                )
            }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        themeWriteGate?.await()
        themeWriteFailure?.let { failure ->
            themeWriteFailure = null
            throw failure
        }
        themeWrites += themeMode
        preferences.value = preferences.value.copy(themeMode = themeMode)
    }

    override suspend fun setReminderConfig(config: ReminderConfig) {
        updates++
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
