package com.leeseungyun1020.manicule.feature.scanner

import androidx.camera.core.ImageAnalysis
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelStore
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.BookSyncResult
import com.leeseungyun1020.manicule.core.domain.scanner.GetBookByScanUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.scanner.BarcodeReader
import com.leeseungyun1020.manicule.core.scanner.BarcodeReaderFactory
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.Executor

@RunWith(JUnit4::class)
class ScannerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val factory = FakeReaderFactory()
    private val savedState = SavedStateHandle()
    private val viewModel = ScannerViewModel(factory, GetBookByScanUseCase(EmptyBookRepository()), StandardTestDispatcher(), savedState)

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
        viewModel.onPreviewReady(viewModel.onPreviewInitializing())
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
            GetBookByScanUseCase(EmptyBookRepository()),
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
        val generation = viewModel.onPreviewInitializing()
        viewModel.onPreviewReady(generation)
        viewModel.onPreviewFailed(generation)
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.PermissionDenied())
        viewModel.onPermissionChanged(true)
        assertThat(viewModel.reader).isSameInstanceAs(reader)
        assertThat(factory.creations).isEqualTo(1)
    }

    @Test
    fun newPreviewLifecycleReinitializesWithoutRecreatingReader() {
        viewModel.onPermissionChanged(true)
        viewModel.onPreviewReady(viewModel.onPreviewInitializing())
        viewModel.onPreviewInitializing()
        assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Initializing)
        viewModel.onPreviewReady(viewModel.onPreviewInitializing())
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
        viewModel.onPreviewFailed(viewModel.onPreviewInitializing())
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

    @Test
    fun activeReadyPreviewStartsOneLookupAndDeliversActualBookIsbnOnce() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel =
                ScannerViewModel(
                    factory,
                    GetBookByScanUseCase(SuccessBookRepository()),
                    StandardTestDispatcher(testScheduler),
                    SavedStateHandle(),
                )
            viewModel.onPermissionChanged(true)
            val generation = viewModel.onPreviewInitializing()
            viewModel.onPreviewReady(generation)
            assertThat(factory.reader.requests).isEqualTo(0)

            viewModel.onActiveChanged(true)
            advanceUntilIdle()
            assertThat(factory.reader.requests).isEqualTo(1)
            assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.LookingUp)

            factory.reader.barcodes.complete(listOf("raw-barcode"))
            advanceUntilIdle()
            assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Success("actual-isbn"))

            assertThat(viewModel.consumeBookNavigation()).isEqualTo("actual-isbn")
            viewModel.onPreviewReady(generation)
            assertThat(factory.reader.requests).isEqualTo(1)
        }

    @Test
    fun inactiveScreenCancelsPendingRecognitionAndIgnoresLateCallbacks() =
        runTest(mainDispatcherRule.dispatcher) {
            viewModel.onPermissionChanged(true)
            viewModel.onActiveChanged(true)
            viewModel.onPreviewReady(viewModel.onPreviewInitializing())
            advanceUntilIdle()

            viewModel.onActiveChanged(false)
            advanceUntilIdle()

            assertThat(factory.reader.cancelled).isTrue()
            assertThat(viewModel.uiState.value).isEqualTo(ScannerUiState.Initializing)
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
        var requests = 0
        var cancelled = false
        val barcodes = CompletableDeferred<List<String>>()
        override val imageAnalysis: ImageAnalysis get() = error("ViewModel must not access use cases")

        override suspend fun getBarcodes(predicate: (String) -> Boolean): List<String> {
            requests++
            try {
                return barcodes.await()
            } finally {
                cancelled = true
            }
        }

        override fun close() {
            closes++
        }
    }

    private class EmptyBookRepository : BookRepository {
        override fun observeBook(isbn: String): Flow<Book?> = emptyFlow()

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> = Result.failure(NoSuchElementException(isbn))

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }

    private class SuccessBookRepository : BookRepository {
        private var synced = false
        private val syncedBook =
            Book(
                isbn = "actual-isbn",
                title = "Title",
                author = "Author",
                publisher = "Publisher",
                publishedDate = null,
                coverUrl = null,
                totalPages = null,
                price = null,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            )

        override fun observeBook(isbn: String): Flow<Book?> =
            flowOf(
                if (synced) {
                    syncedBook
                } else {
                    null
                },
            )

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> {
            synced = true
            return Result.success(BookSyncResult(book = syncedBook, status = BookSyncStatus.COMPLETE))
        }

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }
}
