package com.leeseungyun1020.manicule.feature.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.leeseungyun1020.manicule.core.common.di.Dispatcher
import com.leeseungyun1020.manicule.core.common.di.ManiculeDispatcher
import com.leeseungyun1020.manicule.core.scanner.BarcodeReader
import com.leeseungyun1020.manicule.core.scanner.BarcodeReaderFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ScannerViewModel
    @Inject
    constructor(
        private val readerFactory: BarcodeReaderFactory,
        @Dispatcher(ManiculeDispatcher.Default) private val dispatcher: CoroutineDispatcher,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private var requiresSettings: Boolean
            get() = savedStateHandle[REQUIRES_SETTINGS] ?: false
            set(value) {
                savedStateHandle[REQUIRES_SETTINGS] = value
            }

        private val mutableUiState = MutableStateFlow<ScannerUiState>(
            ScannerUiState.PermissionDenied(requiresSettings = requiresSettings),
        )
        val uiState = mutableUiState.asStateFlow()

        internal var reader: BarcodeReader? = null
            private set

        fun onPermissionChanged(granted: Boolean) {
            if (!granted) {
                val current = mutableUiState.value as? ScannerUiState.PermissionDenied
                mutableUiState.value = ScannerUiState.PermissionDenied(
                    requiresSettings = requiresSettings,
                    launchFailed = current?.launchFailed ?: false,
                )
                return
            }
            requiresSettings = false
            if (mutableUiState.value !is ScannerUiState.PermissionDenied) return
            mutableUiState.value = ScannerUiState.Initializing
            try {
                if (reader == null) reader = readerFactory.create(dispatcher.asExecutor())
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                mutableUiState.value = ScannerUiState.Failed
            }
        }

        fun onPermissionResult(
            granted: Boolean,
            shouldShowRationale: Boolean,
        ) {
            // A false rationale alone also means "never requested". Only a result can require settings.
            requiresSettings = !granted && !shouldShowRationale
            onPermissionChanged(granted)
        }

        fun onPermissionLaunchFailed() {
            val current = mutableUiState.value as? ScannerUiState.PermissionDenied ?: return
            mutableUiState.value = current.copy(launchFailed = true)
        }

        internal fun onPreviewInitializing() {
            if (mutableUiState.value !is ScannerUiState.PermissionDenied) {
                mutableUiState.value = ScannerUiState.Initializing
            }
        }

        internal fun onPreviewReady() {
            if (mutableUiState.value == ScannerUiState.Initializing) mutableUiState.value = ScannerUiState.Scanning
        }

        internal fun onPreviewFailed() {
            if (mutableUiState.value !is ScannerUiState.PermissionDenied) mutableUiState.value = ScannerUiState.Failed
        }

        override fun onCleared() {
            reader?.close()
            reader = null
        }

        private companion object {
            const val REQUIRES_SETTINGS = "scanner.requiresSettings"
        }
    }
