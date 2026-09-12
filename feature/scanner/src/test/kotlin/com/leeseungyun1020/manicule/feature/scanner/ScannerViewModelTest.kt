package com.leeseungyun1020.manicule.feature.scanner

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.scanner.BarcodeReader
import com.leeseungyun1020.manicule.core.scanner.BarcodeReaderFactory
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.Executor

@RunWith(JUnit4::class)
class ScannerViewModelTest {
    private val factory = FakeReaderFactory()
    private val savedState = SavedStateHandle()
    private val viewModel = ScannerViewModel(factory, StandardTestDispatcher(), savedState)

    @Test
    fun permissionExplanationDoesNotCreateReaderOrRequireSettings() {
        viewModel.onPermissionChanged(false)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.PermissionDenied())
        assertThat(factory.creations).isEqualTo(0)
    }

    @Test
    fun grantCreatesOneReaderAndDoesNotRequestBarcodes() {
        viewModel.onPermissionResult(granted = true, shouldShowRationale = false)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Initializing)
        viewModel.onPreviewReady()
        viewModel.onPermissionChanged(true)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Scanning)
        assertThat(factory.creations).isEqualTo(1)
    }

    @Test
    fun denialWithRationaleAllowsAnotherRequest() {
        viewModel.onPermissionResult(granted = false, shouldShowRationale = true)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.PermissionDenied())
    }

    @Test
    fun denialWithoutRationaleRequiresSettingsAndRestoresDecision() {
        viewModel.onPermissionResult(granted = false, shouldShowRationale = false)
        val restored = ScannerViewModel(
            factory,
            StandardTestDispatcher(),
            SavedStateHandle(
                mapOf(
                    "scanner.requiresSettings" to savedState.get<Boolean>("scanner.requiresSettings"),
                ),
            ),
        )
        assertThat(restored.uiState.value).isEqualTo(ScannerUiState.PermissionDenied(requiresSettings = true))
        assertThat(factory.creations).isEqualTo(0)
    }

    @Test
    fun returningFromSettingsRechecksPermissionAndClearsPermanentDenial() {
        viewModel.onPermissionResult(granted = false, shouldShowRationale = false)
        viewModel.onPermissionChanged(true)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Initializing)
        viewModel.onPermissionChanged(false)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.PermissionDenied())
    }

    @Test
    fun settingsLaunchFailureKeepsPermissionExplanationOnResume() {
        viewModel.onPermissionResult(granted = false, shouldShowRationale = false)
        viewModel.onPermissionLaunchFailed()
        viewModel.onPermissionChanged(false)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.PermissionDenied(requiresSettings = true, launchFailed = true))
    }

    @Test
    fun permissionLossIgnoresPreviewCallbacksAndReusesReaderOnRegrant() {
        viewModel.onPermissionChanged(true)
        val reader = viewModel.reader
        viewModel.onPermissionChanged(false)
        viewModel.onPreviewInitializing()
        viewModel.onPreviewReady()
        viewModel.onPreviewFailed()
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.PermissionDenied())
        viewModel.onPermissionChanged(true)
        assertThat(viewModel.reader).isSameInstanceAs(reader)
        assertThat(factory.creations).isEqualTo(1)
    }

    @Test
    fun newPreviewLifecycleReinitializesWithoutRecreatingReader() {
        viewModel.onPermissionChanged(true)
        viewModel.onPreviewReady()
        viewModel.onPreviewInitializing()
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Initializing)
        viewModel.onPreviewReady()
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Scanning)
        assertThat(factory.creations).isEqualTo(1)
    }

    @Test
    fun readerCreationAndBindingFailuresShowFailure() {
        factory.failure = IllegalStateException("unavailable")
        viewModel.onPermissionChanged(true)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Failed)
        assertThat(viewModel.reader).isNull()
        viewModel.onPermissionChanged(false)
        factory.failure = null
        viewModel.onPermissionChanged(true)
        viewModel.onPreviewFailed()
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Failed)
    }

    @Test
    fun clearingViewModelClosesReaderExactlyOnce() {
        val store = ViewModelStore()
        store.put("scanner", viewModel)
        viewModel.onPermissionChanged(true)
        store.clear()
        store.clear()
        assertThat(factory.reader.closes).isEqualTo(1)
        assertThat(viewModel.reader).isNull()
    }

    private class FakeReaderFactory : BarcodeReaderFactory {
        val reader = FakeReader()
        var creations = 0
        var failure: Exception? = null

        override fun create(executor: Executor): BarcodeReader {
            creations++
            failure?.let { throw it }
            return reader
        }
    }

    private class FakeReader : BarcodeReader {
        var closes = 0
        override val imageAnalysis: ImageAnalysis get() = error("ViewModel must not access use cases")

        override suspend fun getBarcodes(predicate: (String) -> Boolean): List<String> = error("Recognition is outside this PR")

        override fun close() {
            closes++
        }
    }
}
