package com.leeseungyun1020.manicule.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.settings.GetUserPreferencesUseCase
import com.leeseungyun1020.manicule.core.domain.settings.SetReminderUseCase
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        getUserPreferences: GetUserPreferencesUseCase,
        private val setReminder: SetReminderUseCase,
    ) : ViewModel() {
        private val retryRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        private val isUpdating = MutableStateFlow(false)
        private val _events = MutableSharedFlow<SettingsEvent>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
        private var pendingRetry: SettingsEvent.ReminderUpdateFailed? = null

        val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

        private val preferencesState =
            retryRequests
                .onStart { emit(Unit) }
                .flatMapLatest {
                    getUserPreferences()
                        .map { preferences -> preferences.toPreferencesState() }
                        .onStart { emit(PreferencesState.Loading) }
                        .catch { emit(PreferencesState.Error) }
                }

        val uiState =
            combine(preferencesState, isUpdating) { preferences, updating ->
                when (preferences) {
                    PreferencesState.Loading -> SettingsUiState.Loading
                    PreferencesState.Error -> SettingsUiState.Error
                    is PreferencesState.Content ->
                        SettingsUiState.Content(
                            reminder = preferences.reminder,
                            isUpdating = updating,
                        )
                }
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = SettingsUiState.Loading,
            )

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
            retryRequests.tryEmit(Unit)
        }

        private fun currentReminder(): ReminderConfig? =
            (uiState.value as? SettingsUiState.Content)
                ?.takeUnless { it.isUpdating }
                ?.reminder

        private fun updateReminder(config: ReminderConfig) {
            if (isUpdating.value || uiState.value !is SettingsUiState.Content) return
            if (pendingRetry != null) {
                _events.tryEmit(SettingsEvent.DismissReminderUpdateFailure)
            }
            pendingRetry = null
            _events.resetReplayCache()
            isUpdating.value = true
            viewModelScope.launch {
                try {
                    setReminder(config)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Throwable) {
                    val failure = SettingsEvent.ReminderUpdateFailed(config)
                    pendingRetry = failure
                    _events.tryEmit(failure)
                } finally {
                    isUpdating.value = false
                }
            }
        }
    }

private sealed interface PreferencesState {
    data object Loading : PreferencesState

    data object Error : PreferencesState

    data class Content(
        val reminder: ReminderConfig,
    ) : PreferencesState
}

private fun UserPreferences.toPreferencesState(): PreferencesState = PreferencesState.Content(reminder)
