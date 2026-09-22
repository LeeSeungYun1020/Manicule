package com.leeseungyun1020.manicule.feature.scanner

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.common.di.Dispatcher
import com.leeseungyun1020.manicule.core.common.di.ManiculeDispatcher
import com.leeseungyun1020.manicule.core.domain.scanner.GetBookByScanUseCase
import com.leeseungyun1020.manicule.core.scanner.BarcodeReader
import com.leeseungyun1020.manicule.core.scanner.BarcodeReaderFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@Suppress("TooManyFunctions")
class ScannerViewModel
    @Inject
    constructor(
        private val readerFactory: BarcodeReaderFactory,
        private val getBookByScan: GetBookByScanUseCase,
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

        private var previewGeneration = 0L
        private var active = false
        private var permissionGranted = false
        private var previewReady = false
        private var recognitionJobGeneration = 0L
        private var recognitionJob: Job? = null

        fun onPermissionChanged(granted: Boolean) {
            permissionGranted = granted
            if (!granted) {
                cancelRecognition()
                previewReady = false
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
                mutableUiState.value = ScannerUiState.CameraUnavailable
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

        internal fun onPreviewInitializing(): Long {
            previewGeneration += 1
            previewReady = false
            when (mutableUiState.value) {
                ScannerUiState.Scanning -> {
                    cancelRecognition()
                    if (permissionGranted) mutableUiState.value = ScannerUiState.Initializing
                }

                ScannerUiState.Initializing,
                ScannerUiState.LookingUp,
                is ScannerUiState.PermissionDenied,
                is ScannerUiState.Success,
                ScannerUiState.NavigationDelivered,
                ScannerUiState.CameraUnavailable,
                ScannerUiState.Failed,
                -> Unit
            }
            return previewGeneration
        }

        internal fun onPreviewReady(generation: Long) {
            if (generation != previewGeneration || !permissionGranted) return
            previewReady = true
            if (mutableUiState.value == ScannerUiState.Initializing) {
                mutableUiState.value = ScannerUiState.Scanning
            }
            startRecognitionIfReady()
        }

        internal fun onPreviewFailed(generation: Long) {
            if (generation != previewGeneration || !permissionGranted) return
            if (mutableUiState.value == ScannerUiState.LookingUp ||
                mutableUiState.value is ScannerUiState.Success ||
                mutableUiState.value == ScannerUiState.NavigationDelivered
            ) {
                return
            }
            cancelRecognition()
            if (mutableUiState.value !is ScannerUiState.PermissionDenied) {
                mutableUiState.value = ScannerUiState.CameraUnavailable
            }
        }

        internal fun onActiveChanged(isActive: Boolean) {
            active = isActive
            if (!active) {
                cancelRecognition()
                if (mutableUiState.value == ScannerUiState.LookingUp || mutableUiState.value == ScannerUiState.Scanning) {
                    mutableUiState.value = ScannerUiState.Initializing
                }
                return
            }
            if (previewReady && mutableUiState.value == ScannerUiState.Initializing) {
                mutableUiState.value = ScannerUiState.Scanning
            }
            startRecognitionIfReady()
        }

        fun consumeBookNavigation(): String? {
            if (!active) return null
            val success = mutableUiState.value as? ScannerUiState.Success ?: return null
            mutableUiState.value = ScannerUiState.NavigationDelivered
            return success.isbn
        }

        fun onExit() = onActiveChanged(false)

        override fun onCleared() {
            cancelRecognition()
            reader?.close()
            reader = null
        }

        private fun startRecognitionIfReady() {
            if (!canStartRecognition()) return
            val barcodeReader = reader ?: return
            val generation = ++recognitionJobGeneration
            recognitionJob = viewModelScope.launch { recognizeBook(barcodeReader, generation) }
        }

        private fun canStartRecognition(): Boolean =
            active && permissionGranted && previewReady && mutableUiState.value == ScannerUiState.Scanning

        private suspend fun recognizeBook(
            barcodeReader: BarcodeReader,
            generation: Long,
        ) {
            try {
                val candidates = barcodeReader.getBarcodes()
                if (generation != recognitionJobGeneration || !active || !permissionGranted) return
                mutableUiState.value = ScannerUiState.LookingUp
                val isbn = getBookByScan(candidates)
                if (generation != recognitionJobGeneration || !active || !permissionGranted) return
                mutableUiState.value = isbn?.let(ScannerUiState::Success) ?: ScannerUiState.Failed
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                if (generation == recognitionJobGeneration && active && permissionGranted) {
                    mutableUiState.value = ScannerUiState.Failed
                }
            }
        }

        private fun cancelRecognition() {
            recognitionJobGeneration += 1
            recognitionJob?.cancel()
            recognitionJob = null
        }

        private companion object {
            const val REQUIRES_SETTINGS = "scanner.requiresSettings"
        }
    }
