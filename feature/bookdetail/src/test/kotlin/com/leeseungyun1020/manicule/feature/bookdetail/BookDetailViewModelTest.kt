package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.domain.book.GetBookDetailUseCase
import com.leeseungyun1020.manicule.core.domain.library.ChangeReadingStatusUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test

class BookDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var bookRepository: FakeBookRepository
    private lateinit var libraryRepository: FakeLibraryRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        bookRepository = FakeBookRepository()
        libraryRepository = FakeLibraryRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cachedBook_isShown_whenRefreshFails() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            bookRepository.refreshResult = Result.failure(IllegalStateException("offline"))

            val viewModel = createViewModel()
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.book).isEqualTo(testBook)
            assertThat(content.bookDetail.entry).isNull()
            assertThat(content.refreshStatus).isEqualTo(RefreshStatus.Failed)
        }

    @Test
    fun missingBookAndRefreshFailure_isFatalError() =
        runTest(dispatcher) {
            bookRepository.refreshResult = Result.failure(NoSuchElementException())

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value).isEqualTo(BookDetailUiState.Error)
        }

    @Test
    fun missingBookAndAuxiliaryContentFailure_showsContentWithRetry() =
        runTest(dispatcher) {
            bookRepository.refreshResult = Result.success(BookSyncStatus.AUXILIARY_CONTENT_FAILED)
            bookRepository.bookAfterSync = testBook

            val viewModel = createViewModel()
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.book).isEqualTo(testBook)
            assertThat(content.refreshStatus).isEqualTo(RefreshStatus.Failed)
        }

    @Test
    fun databaseUpdateBeforeRefreshSuccess_keepsLatestContent_andEndsIdle() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            bookRepository.refreshGate = CompletableDeferred()

            val viewModel = createViewModel()
            runCurrent()

            var content = contentState(viewModel)
            assertThat(content.bookDetail.book).isEqualTo(testBook)
            assertThat(content.refreshStatus).isEqualTo(RefreshStatus.Refreshing)

            val updatedBook = testBook.copy(title = "Updated Book")
            bookRepository.books.value = updatedBook
            runCurrent()

            content = contentState(viewModel)
            assertThat(content.bookDetail.book).isEqualTo(updatedBook)
            assertThat(content.refreshStatus).isEqualTo(RefreshStatus.Refreshing)

            bookRepository.refreshGate?.complete(Unit)
            advanceUntilIdle()

            content = contentState(viewModel)
            assertThat(content.bookDetail.book).isEqualTo(updatedBook)
            assertThat(content.refreshStatus).isEqualTo(RefreshStatus.Idle)
        }

    @Test
    fun retryFromError_updatesContent_andEndsIdleOnSuccess() =
        runTest(dispatcher) {
            bookRepository.refreshResult = Result.failure(NoSuchElementException())
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value).isEqualTo(BookDetailUiState.Error)

            bookRepository.refreshResult = Result.success(BookSyncStatus.COMPLETE)
            bookRepository.books.value = testBook
            viewModel.retry()
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.book).isEqualTo(testBook)
            assertThat(content.refreshStatus).isEqualTo(RefreshStatus.Idle)
        }

    @Test
    fun retryAfterObservationFailure_resubscribesAndUpdatesContent() =
        runTest(dispatcher) {
            bookRepository.bookFlow =
                flow { throw IllegalStateException("Database observation failed") }

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value).isEqualTo(BookDetailUiState.Error)
            assertThat(bookRepository.observationCount).isEqualTo(1)

            bookRepository.bookFlow = bookRepository.books
            bookRepository.books.value = testBook

            viewModel.retry()
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.book).isEqualTo(testBook)
            assertThat(content.refreshStatus).isEqualTo(RefreshStatus.Idle)
            assertThat(bookRepository.observationCount).isEqualTo(2)
        }

    @Test
    fun libraryEntry_selectsRecordsInitially_butDoesNotOverrideUserSelection() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()
            assertThat(contentState(viewModel).selectedTab).isEqualTo(BookDetailTab.MyRecords)

            viewModel.selectTab(BookDetailTab.Information)
            libraryRepository.entry.value = null
            libraryRepository.entry.value = testEntry
            advanceUntilIdle()

            assertThat(contentState(viewModel).selectedTab).isEqualTo(BookDetailTab.Information)
            assertThat(savedStateHandle.get<String>("bookDetailSelectedTab")).isEqualTo(BookDetailTab.Information.name)
        }

    @Test
    fun reviewOnlyEntry_selectsRecordsInitially_whenStatusIsUnset() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value =
                testEntry.copy(
                    status = ReadingStatus.UNSET,
                    rating = 4,
                    memo = "Review only",
                )

            val viewModel = createViewModel()
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.entry?.status).isEqualTo(ReadingStatus.UNSET)
            assertThat(content.selectedTab).isEqualTo(BookDetailTab.MyRecords)
        }

    @Test
    fun openMyRecords_selectsRecordsTab() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook

            val viewModel = createViewModel(createSavedStateHandle(openMyRecords = true))
            advanceUntilIdle()

            assertThat(contentState(viewModel).selectedTab).isEqualTo(BookDetailTab.MyRecords)
        }

    @Test
    fun restoredTab_takesPriorityOverOpenMyRecords() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook

            val viewModel =
                createViewModel(
                    createSavedStateHandle(
                        openMyRecords = true,
                        savedTab = BookDetailTab.Information,
                    ),
                )
            advanceUntilIdle()

            assertThat(contentState(viewModel).selectedTab).isEqualTo(BookDetailTab.Information)
        }

    @Test
    fun statusChange_registersBook_withoutSwitchingSelectedTab() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.changeReadingStatus(ReadingStatus.WANT)
            advanceUntilIdle()
            assertThat(contentState(viewModel).bookDetail.entry?.status).isEqualTo(ReadingStatus.WANT)
            assertThat(contentState(viewModel).selectedTab).isEqualTo(BookDetailTab.Information)
            assertThat(contentState(viewModel).statusChange).isEqualTo(StatusChangeState.Idle)
        }

    @Test
    fun saving_blocksDuplicateRequests_andSurvivesContentUpdates() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry
            libraryRepository.statusGate = CompletableDeferred()
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.changeReadingStatus(ReadingStatus.FINISHED)
            viewModel.changeReadingStatus(ReadingStatus.WANT)
            runCurrent()
            bookRepository.books.value = testBook.copy(title = "Refreshed")
            viewModel.selectTab(BookDetailTab.Information)
            runCurrent()
            assertThat(contentState(viewModel).statusChange).isEqualTo(StatusChangeState.Saving(ReadingStatus.FINISHED))
            assertThat(contentState(viewModel).bookDetail.entry?.status).isEqualTo(ReadingStatus.READING)
            assertThat(libraryRepository.statusCalls).isEqualTo(1)
            libraryRepository.statusGate?.complete(Unit)
            advanceUntilIdle()
            assertThat(contentState(viewModel).bookDetail.entry?.status).isEqualTo(ReadingStatus.FINISHED)
            assertThat(contentState(viewModel).statusChange).isEqualTo(StatusChangeState.Idle)
        }

    @Test
    fun failedSave_keepsReviewAndStatus_andCanRetryRepeatedFailures() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            val entry = testEntry.copy(status = ReadingStatus.UNSET, rating = 4, memo = "Keep")
            libraryRepository.entry.value = entry
            libraryRepository.statusFailure = IllegalStateException("Disk error")
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.changeReadingStatus(ReadingStatus.READING)
            advanceUntilIdle()
            val firstFailure = contentState(viewModel).statusChange as StatusChangeState.Failed
            assertThat(contentState(viewModel).bookDetail.entry).isEqualTo(entry)
            viewModel.changeReadingStatus(firstFailure.target)
            advanceUntilIdle()
            val secondFailure = contentState(viewModel).statusChange as StatusChangeState.Failed
            assertThat(secondFailure.attempt).isGreaterThan(firstFailure.attempt)
            libraryRepository.statusFailure = null
            viewModel.changeReadingStatus(secondFailure.target)
            advanceUntilIdle()
            assertThat(contentState(viewModel).statusChange).isEqualTo(StatusChangeState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry?.rating).isEqualTo(4)
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isEqualTo("Keep")
        }

    @Test
    fun missingBookFailure_isDismissible_andDoesNotAlterRefreshFailure() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            bookRepository.refreshResult = Result.failure(IllegalStateException())
            libraryRepository.statusResult = ReadingStatusChangeResult.BookNotFound
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.changeReadingStatus(ReadingStatus.WANT)
            advanceUntilIdle()
            assertThat(contentState(viewModel).statusChange).isInstanceOf(StatusChangeState.Failed::class.java)
            viewModel.dismissStatusError()
            assertThat(contentState(viewModel).statusChange).isEqualTo(StatusChangeState.Idle)
            assertThat(contentState(viewModel).refreshStatus).isEqualTo(RefreshStatus.Failed)
            assertThat(contentState(viewModel).bookDetail.entry).isNull()
        }

    @Test
    fun success_waitsForObservedEntry_insteadOfInventingUiData() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.emitStatus = false
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.changeReadingStatus(ReadingStatus.WANT)
            advanceUntilIdle()
            assertThat(contentState(viewModel).bookDetail.entry).isNull()
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.WANT)
            advanceUntilIdle()
            assertThat(contentState(viewModel).bookDetail.entry?.status).isEqualTo(ReadingStatus.WANT)
        }

    @Test
    fun unsetAndLoading_ignoreStatusRequests() =
        runTest(dispatcher) {
            bookRepository.refreshGate = CompletableDeferred()
            val viewModel = createViewModel()
            viewModel.changeReadingStatus(ReadingStatus.WANT)
            bookRepository.books.value = testBook
            bookRepository.refreshGate?.complete(Unit)
            advanceUntilIdle()
            viewModel.changeReadingStatus(ReadingStatus.UNSET)
            advanceUntilIdle()
            assertThat(libraryRepository.statusCalls).isEqualTo(0)
        }

    @Test
    fun cancellation_doesNotShowSaveError_orKeepButtonsDisabled() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.statusFailure = CancellationException("Cancelled")
            val viewModel = createViewModel()
            advanceUntilIdle()
            viewModel.changeReadingStatus(ReadingStatus.WANT)
            advanceUntilIdle()
            assertThat(contentState(viewModel).statusChange).isEqualTo(StatusChangeState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry).isNull()
        }

    private fun createSavedStateHandle(
        openMyRecords: Boolean = false,
        savedTab: BookDetailTab? = null,
    ): SavedStateHandle {
        val state =
            mutableMapOf<String, Any?>(
                "isbn" to "123",
                "openMyRecords" to openMyRecords,
            )
        savedTab?.let { state["bookDetailSelectedTab"] = it.name }
        return SavedStateHandle(state)
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = createSavedStateHandle()): BookDetailViewModel =
        BookDetailViewModel(
            getBookDetail = GetBookDetailUseCase(bookRepository, libraryRepository),
            changeStatus = ChangeReadingStatusUseCase(
                libraryRepository,
                object : Clock {
                    override fun now(): Instant = Instant.fromEpochMilliseconds(100)

                    override fun timeZone(): TimeZone = TimeZone.UTC
                },
            ),
            savedStateHandle = savedStateHandle,
        )

    private fun contentState(viewModel: BookDetailViewModel): BookDetailUiState.Content {
        val state = viewModel.uiState.value
        assertThat(state).isInstanceOf(BookDetailUiState.Content::class.java)
        return state as BookDetailUiState.Content
    }

    private class FakeBookRepository : BookRepository {
        val books = MutableStateFlow<Book?>(null)
        var bookFlow: Flow<Book?> = books
        var observationCount = 0
        var refreshResult: Result<BookSyncStatus> = Result.success(BookSyncStatus.COMPLETE)
        var refreshGate: CompletableDeferred<Unit>? = null
        var bookAfterSync: Book? = null

        override fun observeBook(isbn: String): Flow<Book?> =
            flow {
                observationCount++
                emitAll(bookFlow)
            }

        override suspend fun syncBook(isbn: String): Result<BookSyncStatus> {
            refreshGate?.await()
            bookAfterSync?.let { books.value = it }
            return refreshResult
        }

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }

    private class FakeLibraryRepository : LibraryRepository {
        var statusCalls = 0
        var statusGate: CompletableDeferred<Unit>? = null
        var statusFailure: Exception? = null
        var statusResult = ReadingStatusChangeResult.Changed
        var emitStatus = true
        val entry = MutableStateFlow<BookEntry?>(null)

        override fun observeAll(): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: kotlinx.datetime.Instant,
            finishedAt: kotlinx.datetime.LocalDate?,
        ): ReadingStatusChangeResult {
            statusCalls++
            statusGate?.await()
            statusFailure?.let { throw it }
            if (emitStatus && statusResult == ReadingStatusChangeResult.Changed) {
                entry.value = (entry.value ?: testEntry.copy(addedAt = updatedAt)).copy(
                    status = status,
                    updatedAt = updatedAt,
                    finishedAt = finishedAt,
                )
            }
            return statusResult
        }

        override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> = emptyList()

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> = entry

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = SaveBookEntryResult.Saved

        override suspend fun removeBookEntry(isbn: String) = Unit
    }

    private companion object {
        val testBook =
            Book(
                isbn = "123",
                title = "Book",
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
        val testEntry =
            BookEntry(
                book = testBook,
                status = ReadingStatus.READING,
                addedAt = Instant.fromEpochMilliseconds(1),
                updatedAt = Instant.fromEpochMilliseconds(1),
            )
    }
}
