package com.leeseungyun1020.manicule.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.settings.GetUserPreferencesUseCase
import com.leeseungyun1020.manicule.core.domain.settings.SetReminderUseCase
import com.leeseungyun1020.manicule.core.domain.settings.SetThemeUseCase
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        getUserPreferences: GetUserPreferencesUseCase,
        private val setReminder: SetReminderUseCase,
        private val setTheme: SetThemeUseCase,
    ) : ViewModel() {
        private val retryRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        private var isUpdating = false
        private var retryRequested = false
        private var readGeneration = 0
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState = _uiState.asStateFlow()
        private val _events = MutableSharedFlow<SettingsEvent>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        private var pendingRetry: SettingsEvent.ReminderUpdateFailed? = null
        private val themeUpdates = Channel<Pair<Long, ThemeMode>>(Channel.UNLIMITED)
        private var themeSelectionGeneration = 0L
        private val _themeEvents = MutableSharedFlow<ThemeEvent>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        private var pendingThemeRetry: ThemeEvent.UpdateFailed? = null

        val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()
        val themeEvents: SharedFlow<ThemeEvent> = _themeEvents.asSharedFlow()

        init {
            viewModelScope.launch {
                retryRequests
                    .onStart { emit(Unit) }
                    .flatMapLatest {
                        getUserPreferences()
                            .onStart {
                                retryRequested = false
                                _uiState.update { state ->
                                    state.copy(
                                        reminder = ReminderUiState.Loading(state.reminder.displayedReminder),
                                        theme = ThemeUiState.Loading(state.theme.displayedMode),
                                    )
                                }
                            }
                            .catch {
                                readGeneration++
                                invalidateReminderRetry()
                                if (pendingThemeRetry != null) _themeEvents.tryEmit(ThemeEvent.DismissUpdateFailure)
                                pendingThemeRetry?.let { resolveThemeUpdateFailure(it, retry = false) }
                                _uiState.update { state ->
                                    state.copy(
                                        reminder = ReminderUiState.Error(state.reminder.displayedReminder),
                                        theme = ThemeUiState.Error(state.theme.displayedMode),
                                    )
                                }
                            }
                    }
                    .collect { preferences ->
                        _uiState.update { state ->
                            state.copy(
                                reminder = ReminderUiState.Content(preferences.reminder, isUpdating),
                                theme = ThemeUiState.Content(preferences.themeMode),
                            )
                        }
                    }
            }
            viewModelScope.launch {
                for ((selection, mode) in themeUpdates) {
                    val generation = readGeneration
                    try {
                        setTheme(mode)
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (_: Throwable) {
                        if (generation == readGeneration &&
                            selection == themeSelectionGeneration &&
                            uiState.value.theme is ThemeUiState.Content
                        ) {
                            val failure = ThemeEvent.UpdateFailed(mode)
                            pendingThemeRetry = failure
                            _themeEvents.tryEmit(failure)
                        }
                    }
                }
            }
        }

        fun setThemeMode(mode: ThemeMode) {
            if (uiState.value.theme !is ThemeUiState.Content) return
            if (pendingThemeRetry != null) _themeEvents.tryEmit(ThemeEvent.DismissUpdateFailure)
            pendingThemeRetry?.let { resolveThemeUpdateFailure(it, retry = false) }
            themeUpdates.trySend(++themeSelectionGeneration to mode)
        }

        fun resolveThemeUpdateFailure(
            failure: ThemeEvent.UpdateFailed,
            retry: Boolean,
        ) {
            if (pendingThemeRetry !== failure) return
            if (retry) _themeEvents.tryEmit(ThemeEvent.DismissUpdateFailure)
            pendingThemeRetry = null
            _themeEvents.resetReplayCache()
            if (retry && uiState.value.theme is ThemeUiState.Content) {
                themeUpdates.trySend(++themeSelectionGeneration to failure.desiredMode)
            }
        }

        fun setReminderEnabled(enabled: Boolean) {
            val current = currentReminder() ?: return
            updateReminder(current.copy(enabled = enabled))
        }

        fun setReminderTime(time: LocalTime) {
            val current = currentReminder() ?: return
            updateReminder(current.copy(time = time))
        }

        fun retryReminderUpdate(failure: SettingsEvent.ReminderUpdateFailed) {
            if (pendingRetry !== failure) return
            updateReminder(failure.desiredConfig)
        }

        fun dismissReminderUpdateFailure(failure: SettingsEvent.ReminderUpdateFailed) {
            if (pendingRetry === failure) {
                pendingRetry = null
                _events.resetReplayCache()
            }
        }

        fun retryPreferences() {
            if (retryRequested || (uiState.value.reminder !is ReminderUiState.Error && uiState.value.theme !is ThemeUiState.Error)) return
            retryRequested = retryRequests.tryEmit(Unit)
        }

        private fun currentReminder(): ReminderConfig? =
            (uiState.value.reminder as? ReminderUiState.Content)
                ?.takeUnless { it.isUpdating }
                ?.reminder

        private fun invalidateReminderRetry() {
            if (pendingRetry != null) {
                _events.tryEmit(SettingsEvent.DismissReminderUpdateFailure)
            }
            pendingRetry = null
            _events.resetReplayCache()
        }

        private fun updateReminder(config: ReminderConfig) {
            if (isUpdating || currentReminder() == null) return
            invalidateReminderRetry()
            isUpdating = true
            val generation = readGeneration
            _uiState.update { state ->
                val reminder = state.reminder as? ReminderUiState.Content ?: return@update state
                state.copy(reminder = reminder.copy(isUpdating = true))
            }
            viewModelScope.launch {
                try {
                    setReminder(config)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Throwable) {
                    if (generation == readGeneration && uiState.value.reminder is ReminderUiState.Content) {
                        val failure = SettingsEvent.ReminderUpdateFailed(config)
                        pendingRetry = failure
                        _events.tryEmit(failure)
                    }
                } finally {
                    isUpdating = false
                    _uiState.update { state ->
                        val reminder = state.reminder as? ReminderUiState.Content ?: return@update state
                        state.copy(reminder = reminder.copy(isUpdating = false))
                    }
                }
            }
        }
    }
