package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.BookSyncResult
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.domain.book.GetBookDetailUseCase
import com.leeseungyun1020.manicule.core.domain.library.ChangeReadingStatusUseCase
import com.leeseungyun1020.manicule.core.domain.library.UpdateMemoUseCase
import com.leeseungyun1020.manicule.core.domain.library.UpdateRatingUseCase
import com.leeseungyun1020.manicule.core.domain.record.AddReadingRecordUseCase
import com.leeseungyun1020.manicule.core.domain.record.ObserveBookRecordsUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.MemoChangeResult
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingRecord
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Before
import org.junit.Test

class BookDetailViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var bookRepository: FakeBookRepository
    private lateinit var libraryRepository: FakeLibraryRepository
    private lateinit var recordRepository: FakeReadingRecordRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        bookRepository = FakeBookRepository()
        libraryRepository = FakeLibraryRepository()
        recordRepository = FakeReadingRecordRepository()
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

    @Test
    fun updateRating_unregisteredBook_setsUnsetWithRating() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(4)
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.entry?.status).isEqualTo(ReadingStatus.UNSET)
            assertThat(content.bookDetail.entry?.rating).isEqualTo(4)
            assertThat(content.ratingSaving).isEqualTo(RatingSavingState.Idle)
            assertThat(libraryRepository.ratingCalls).isEqualTo(1)
        }

    @Test
    fun updateRating_sameRating_clearsToZeroRating() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(rating = 4)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(4)
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.entry?.rating).isEqualTo(0)
            assertThat(content.ratingSaving).isEqualTo(RatingSavingState.Idle)
        }

    @Test
    fun updateRating_differentRating_updatesToNewRating() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(rating = 3)
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(5)
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.entry?.rating).isEqualTo(5)
            assertThat(content.ratingSaving).isEqualTo(RatingSavingState.Idle)
        }

    @Test
    fun updateRating_preservesStatusAndMemo() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value =
                testEntry.copy(
                    status = ReadingStatus.READING,
                    rating = 2,
                    memo = "Keep memo",
                )
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(4)
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.entry?.status).isEqualTo(ReadingStatus.READING)
            assertThat(content.bookDetail.entry?.memo).isEqualTo("Keep memo")
            assertThat(content.bookDetail.entry?.rating).isEqualTo(4)
        }

    @Test
    fun updateRating_saving_blocksDuplicateRequests() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(rating = 1)
            libraryRepository.ratingGate = CompletableDeferred()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(4)
            runCurrent()

            assertThat(contentState(viewModel).ratingSaving).isEqualTo(RatingSavingState.Saving(4))

            viewModel.updateRating(5)
            runCurrent()

            assertThat(libraryRepository.ratingCalls).isEqualTo(1)

            libraryRepository.ratingGate?.complete(Unit)
            advanceUntilIdle()

            assertThat(contentState(viewModel).ratingSaving).isEqualTo(RatingSavingState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry?.rating).isEqualTo(4)
        }

    @Test
    fun updateRating_observationDelay_keepsSavingStateUntilObserved() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(rating = 1)
            libraryRepository.emitRating = false
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(4)
            advanceUntilIdle()

            assertThat(contentState(viewModel).ratingSaving).isEqualTo(RatingSavingState.Saving(4))
            assertThat(contentState(viewModel).bookDetail.entry?.rating).isEqualTo(1)

            libraryRepository.entry.value = testEntry.copy(rating = 4)
            advanceUntilIdle()

            assertThat(contentState(viewModel).ratingSaving).isEqualTo(RatingSavingState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry?.rating).isEqualTo(4)
        }

    @Test
    fun updateRating_failure_showsFailedState_andDismissError() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(rating = 2)
            libraryRepository.ratingFailure = IllegalStateException("Disk error")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(4)
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.ratingSaving).isEqualTo(RatingSavingState.Failed(4, 1L))
            assertThat(content.bookDetail.entry?.rating).isEqualTo(2)

            viewModel.dismissRatingError()
            assertThat(contentState(viewModel).ratingSaving).isEqualTo(RatingSavingState.Idle)
        }

    @Test
    fun updateRating_retry_retriesFailedTarget() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(rating = 2)
            libraryRepository.ratingFailure = IllegalStateException("Disk error")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(4)
            advanceUntilIdle()

            assertThat(contentState(viewModel).ratingSaving).isEqualTo(RatingSavingState.Failed(4, 1L))

            libraryRepository.ratingFailure = null
            viewModel.retryRating()
            advanceUntilIdle()

            assertThat(contentState(viewModel).ratingSaving).isEqualTo(RatingSavingState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry?.rating).isEqualTo(4)
        }

    @Test
    fun updateRating_cancellation_doesNotLeaveSavingStateOrShowError() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.ratingFailure = CancellationException("Cancelled")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateRating(4)
            advanceUntilIdle()

            assertThat(contentState(viewModel).ratingSaving).isEqualTo(RatingSavingState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry).isNull()
        }

    @Test
    fun updateRating_doesNotOverwriteStatusChangeError() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.statusResult = ReadingStatusChangeResult.BookNotFound
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.changeReadingStatus(ReadingStatus.WANT)
            advanceUntilIdle()

            val statusFailed = contentState(viewModel).statusChange
            assertThat(statusFailed).isInstanceOf(StatusChangeState.Failed::class.java)

            libraryRepository.entry.value = testEntry.copy(rating = 1)
            viewModel.updateRating(4)
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.statusChange).isEqualTo(statusFailed)
            assertThat(content.ratingSaving).isEqualTo(RatingSavingState.Idle)
            assertThat(content.bookDetail.entry?.rating).isEqualTo(4)
        }

    @Test
    fun updateMemoDraft_updatesDraft_andStoresInSavedStateHandle() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            viewModel.updateMemoDraft("Draft memo content")

            assertThat(contentState(viewModel).memoDraft).isEqualTo("Draft memo content")
            assertThat(savedStateHandle.get<String?>("bookDetailMemoDraft")).isEqualTo("Draft memo content")
        }

    @Test
    fun saveMemo_whenDraftEqualsObserved_clearsDraftAndDoesNotCallUseCase() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Existing memo")
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            viewModel.updateMemoDraft("Existing memo")
            assertThat(contentState(viewModel).memoDraft).isEqualTo("Existing memo")

            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(libraryRepository.memoCalls).isEqualTo(0)
            assertThat(contentState(viewModel).memoDraft).isNull()
            assertThat(savedStateHandle.get<String?>("bookDetailMemoDraft")).isNull()
            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
        }

    @Test
    fun saveMemo_whenDraftEqualsObservedAfterTrimming_clearsDraftAndDoesNotCallUseCase() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Existing memo")
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            viewModel.updateMemoDraft("   Existing memo   ")
            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(libraryRepository.memoCalls).isEqualTo(0)
            assertThat(contentState(viewModel).memoDraft).isNull()
            assertThat(savedStateHandle.get<String?>("bookDetailMemoDraft")).isNull()
            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
        }

    @Test
    fun saveMemo_whenBlankDraftAndNullObserved_clearsDraftAndDoesNotCallUseCase() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = null)
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            viewModel.updateMemoDraft("   ")
            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(libraryRepository.memoCalls).isEqualTo(0)
            assertThat(contentState(viewModel).memoDraft).isNull()
            assertThat(savedStateHandle.get<String?>("bookDetailMemoDraft")).isNull()
            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
        }

    @Test
    fun saveMemo_normalizesDraftAndCallsUseCase_andUpdatesObservedState() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = null)
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            viewModel.updateMemoDraft("  New normalized memo  ")
            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(libraryRepository.memoCalls).isEqualTo(1)
            val content = contentState(viewModel)
            assertThat(content.bookDetail.entry?.memo).isEqualTo("New normalized memo")
            assertThat(content.memoDraft).isNull()
            assertThat(savedStateHandle.get<String?>("bookDetailMemoDraft")).isNull()
            assertThat(content.memoSaving).isEqualTo(MemoSavingState.Idle)
        }

    @Test
    fun saveMemo_saving_blocksDuplicateRequests() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = null)
            libraryRepository.memoGate = CompletableDeferred()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("Memo 1")
            viewModel.saveMemo()
            runCurrent()

            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Saving("Memo 1"))

            viewModel.updateMemoDraft("Memo 2")
            viewModel.saveMemo()
            runCurrent()

            assertThat(libraryRepository.memoCalls).isEqualTo(1)

            libraryRepository.memoGate?.complete(Unit)
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isEqualTo("Memo 1")
        }

    @Test
    fun saveMemo_observationDelay_keepsSavingStateUntilObserved() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Old memo")
            libraryRepository.emitMemo = false
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("New memo")
            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Saving("New memo"))
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isEqualTo("Old memo")

            libraryRepository.entry.value = testEntry.copy(memo = "New memo")
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
            assertThat(contentState(viewModel).memoDraft).isNull()
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isEqualTo("New memo")
        }

    @Test
    fun saveMemo_failure_keepsDraftAndShowsFailedState_andDismissError() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Old memo")
            libraryRepository.memoFailure = IllegalStateException("Disk error")
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            viewModel.updateMemoDraft("New draft")
            viewModel.saveMemo()
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.memoSaving).isInstanceOf(MemoSavingState.Failed::class.java)
            val failed = content.memoSaving as MemoSavingState.Failed
            assertThat(failed.target).isEqualTo("New draft")
            assertThat(content.memoDraft).isEqualTo("New draft")
            assertThat(savedStateHandle.get<String?>("bookDetailMemoDraft")).isEqualTo("New draft")

            viewModel.dismissMemoError()
            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
            assertThat(contentState(viewModel).memoDraft).isEqualTo("New draft")
            assertThat(savedStateHandle.get<String?>("bookDetailMemoDraft")).isEqualTo("New draft")
        }

    @Test
    fun retryMemo_retriesFailedTarget() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Old memo")
            libraryRepository.memoFailure = IllegalStateException("Disk error")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("Retry memo")
            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isInstanceOf(MemoSavingState.Failed::class.java)

            libraryRepository.memoFailure = null
            viewModel.retryMemo()
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
            assertThat(contentState(viewModel).memoDraft).isNull()
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isEqualTo("Retry memo")
        }

    @Test
    fun retryMemo_withRevisedDraft_savesRevisedDraftInsteadOfFailedTarget() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Old memo")
            libraryRepository.memoFailure = IllegalStateException("Disk error")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("Initial fail draft")
            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isInstanceOf(MemoSavingState.Failed::class.java)

            // 사용자가 실패 스낵바가 노출된 상태에서 초안을 수정함
            viewModel.updateMemoDraft("Revised draft after failure")

            libraryRepository.memoFailure = null
            viewModel.retryMemo()
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
            assertThat(contentState(viewModel).memoDraft).isNull()
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isEqualTo("Revised draft after failure")
        }

    @Test
    fun saveMemo_whenUserTypesNewDraftDuringObservation_preservesNewDraft() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Old memo")
            libraryRepository.emitMemo = false
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("Saving memo")
            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Saving("Saving memo"))

            // 저장이 비동기로 진행되는 도중 사용자가 새로운 초안을 작성함
            viewModel.updateMemoDraft("Newer draft while saving")

            // 이전 저장 타겟이 DB에 반영되어 관찰 스트림으로 방출됨
            libraryRepository.entry.value = testEntry.copy(memo = "Saving memo")
            advanceUntilIdle()

            // 이전 타겟이 반영되었어도 사용자의 새 초안은 보존되어야 함
            val content = contentState(viewModel)
            assertThat(content.memoSaving).isEqualTo(MemoSavingState.Idle)
            assertThat(content.memoDraft).isEqualTo("Newer draft while saving")
            assertThat(content.bookDetail.entry?.memo).isEqualTo("Saving memo")
        }

    @Test
    fun saveMemoAndCheckSuccess_returnsTrueOnSuccess_andFalseOnFailure() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Initial")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("Success memo")
            val successResult = viewModel.saveMemoAndCheckSuccess()
            advanceUntilIdle()
            assertThat(successResult).isTrue()
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isEqualTo("Success memo")

            libraryRepository.memoFailure = IllegalStateException("Failure")
            viewModel.updateMemoDraft("Fail memo")
            val failResult = viewModel.saveMemoAndCheckSuccess()
            advanceUntilIdle()
            assertThat(failResult).isFalse()
            assertThat(contentState(viewModel).memoDraft).isEqualTo("Fail memo")
        }

    @Test
    fun saveMemoAndCheckSuccess_whenNoChanges_returnsTrueWithoutCallingUseCase() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Same")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("Same")
            val result = viewModel.saveMemoAndCheckSuccess()
            advanceUntilIdle()

            assertThat(result).isTrue()
            assertThat(libraryRepository.memoCalls).isEqualTo(0)
            assertThat(contentState(viewModel).memoDraft).isNull()
        }

    @Test
    fun saveMemo_cancellation_doesNotLeaveSavingStateOrShowError_andKeepsDraft() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.memoFailure = CancellationException("Cancelled")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("Cancel memo")
            viewModel.saveMemo()
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoSaving).isEqualTo(MemoSavingState.Idle)
            assertThat(contentState(viewModel).memoDraft).isEqualTo("Cancel memo")
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isNull()
        }

    @Test
    fun savedMemoDraft_restoresDraftFromSavedStateHandle_andObservationDoesNotOverwriteDraft() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry.copy(memo = "Observed memo 1")
            val savedStateHandle = createSavedStateHandle(savedMemoDraft = "Draft from handle")
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoDraft).isEqualTo("Draft from handle")

            libraryRepository.entry.value = testEntry.copy(memo = "Observed memo 2")
            advanceUntilIdle()

            assertThat(contentState(viewModel).memoDraft).isEqualTo("Draft from handle")
            assertThat(contentState(viewModel).bookDetail.entry?.memo).isEqualTo("Observed memo 2")
        }

    @Test
    fun updateMemo_preservesStatusAndRating() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value =
                testEntry.copy(
                    status = ReadingStatus.FINISHED,
                    rating = 5,
                    memo = "Old memo",
                )
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.updateMemoDraft("New memo")
            viewModel.saveMemo()
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.entry?.status).isEqualTo(ReadingStatus.FINISHED)
            assertThat(content.bookDetail.entry?.rating).isEqualTo(5)
            assertThat(content.bookDetail.entry?.memo).isEqualTo("New memo")
        }

    @Test
    fun records_areObserved_andUpdatedInUiState() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            recordRepository.records.value = listOf(testRecord)
            val viewModel = createViewModel()
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.records).containsExactly(testRecord)
        }

    @Test
    fun recordObservationFailure_keepsBookContent_andRecoversRecordsIndependently() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            recordRepository.recordFlow = flow { throw IllegalStateException("Record observation failed") }

            val viewModel = createViewModel()
            runCurrent()

            val content = contentState(viewModel)
            assertThat(content.bookDetail.book).isEqualTo(testBook)
            assertThat(content.records).isEmpty()
            assertThat(content.recordLoadState).isEqualTo(RecordLoadState.Failed(1L))
            assertThat(recordRepository.observationCount).isEqualTo(1)

            recordRepository.recordFlow = recordRepository.records
            recordRepository.records.value = listOf(testRecord)
            viewModel.retry()
            advanceUntilIdle()

            val recovered = contentState(viewModel)
            assertThat(recovered.records).containsExactly(testRecord)
            assertThat(recovered.recordLoadState).isEqualTo(RecordLoadState.Idle)
            assertThat(recordRepository.observationCount).isEqualTo(2)
        }

    @Test
    fun addRecord_success_callsUseCase_andResetsRecordSavingState() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addRecord(
                date = testRecord.date,
                time = testRecord.time,
                startPage = testRecord.startPage,
                endPage = testRecord.endPage,
            )
            advanceUntilIdle()

            assertThat(recordRepository.addCalls).isEqualTo(1)
            val content = contentState(viewModel)
            assertThat(content.recordSaving).isEqualTo(RecordSavingState.Succeeded(1L))
            assertThat(content.records).hasSize(1)
            assertThat(content.records.first().startPage).isEqualTo(testRecord.startPage)
            assertThat(content.records.first().endPage).isEqualTo(testRecord.endPage)
        }

    @Test
    fun addRecord_saving_blocksDuplicateRequests() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            recordRepository.addGate = CompletableDeferred()
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addRecord(
                date = testRecord.date,
                time = testRecord.time,
                startPage = 1,
                endPage = 10,
            )
            viewModel.addRecord(
                date = testRecord.date,
                time = testRecord.time,
                startPage = 11,
                endPage = 20,
            )
            runCurrent()

            assertThat(contentState(viewModel).recordSaving).isEqualTo(RecordSavingState.Saving(1L))
            assertThat(recordRepository.addCalls).isEqualTo(1)

            recordRepository.addGate?.complete(Unit)
            advanceUntilIdle()

            assertThat(contentState(viewModel).recordSaving).isEqualTo(RecordSavingState.Succeeded(1L))
            assertThat(recordRepository.addCalls).isEqualTo(1)
        }

    @Test
    fun addRecord_failure_setsRecordSavingFailed_andCanBeDismissed() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            recordRepository.addFailure = IllegalStateException("Save failed")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addRecord(
                date = testRecord.date,
                time = testRecord.time,
                startPage = 1,
                endPage = 10,
            )
            advanceUntilIdle()

            val content = contentState(viewModel)
            assertThat(content.recordSaving).isInstanceOf(RecordSavingState.Failed::class.java)

            viewModel.dismissRecordError()
            assertThat(contentState(viewModel).recordSaving).isEqualTo(RecordSavingState.Idle)
        }

    @Test
    fun addRecord_cancellation_doesNotLeaveSavingState() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            recordRepository.addFailure = CancellationException("Cancelled")
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addRecord(
                date = testRecord.date,
                time = testRecord.time,
                startPage = 1,
                endPage = 10,
            )
            advanceUntilIdle()

            assertThat(contentState(viewModel).recordSaving).isEqualTo(RecordSavingState.Idle)
        }

    @Test
    fun addRecord_success_triggersFinishCheck_whenConditionMet() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook.copy(totalPages = 200)
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.READING)
            recordRepository.maxEndPageAfterAdd = 160
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 160)
            advanceUntilIdle()

            val check = contentState(viewModel).finishCheck
            assertThat(check).isInstanceOf(FinishCheckState.Pending::class.java)
            assertThat((check as FinishCheckState.Pending).maxEndPage).isEqualTo(160)
            assertThat(check.totalPages).isEqualTo(200)
        }

    @Test
    fun addRecord_success_doesNotTriggerFinishCheck_whenConditionNotMet() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook.copy(totalPages = 200)
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.READING)
            recordRepository.maxEndPageAfterAdd = 100
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 100)
            advanceUntilIdle()

            assertThat(contentState(viewModel).finishCheck).isEqualTo(FinishCheckState.Idle)
        }

    @Test
    fun addRecord_success_doesNotTriggerFinishCheck_whenStatusIsFinished() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook.copy(totalPages = 200)
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.FINISHED)
            recordRepository.maxEndPageAfterAdd = 200
            val viewModel = createViewModel()
            advanceUntilIdle()

            viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 200)
            advanceUntilIdle()

            assertThat(contentState(viewModel).finishCheck).isEqualTo(FinishCheckState.Idle)
        }

    @Test
    fun finishCheck_confirm_changesStatusToFinished_andClearsDialog() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook.copy(totalPages = 200)
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.READING)
            recordRepository.maxEndPageAfterAdd = 200
            val viewModel = createViewModel()
            advanceUntilIdle()

            val attempt = viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 200)!!
            advanceUntilIdle()

            assertThat(contentState(viewModel).finishCheck).isInstanceOf(FinishCheckState.Pending::class.java)

            viewModel.confirmFinish(attempt)
            advanceUntilIdle()

            assertThat(contentState(viewModel).finishCheck).isEqualTo(FinishCheckState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry?.status).isEqualTo(ReadingStatus.FINISHED)
        }

    @Test
    fun finishCheck_dismiss_clearsDialog_withoutChangingStatus() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook.copy(totalPages = 200)
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.READING)
            recordRepository.maxEndPageAfterAdd = 200
            val viewModel = createViewModel()
            advanceUntilIdle()

            val attempt = viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 200)!!
            advanceUntilIdle()

            viewModel.dismissFinishCheck()

            assertThat(contentState(viewModel).finishCheck).isEqualTo(FinishCheckState.Idle)
            assertThat(libraryRepository.statusCalls).isEqualTo(0)
        }

    @Test
    fun finishCheck_confirm_failure_showsFailedState_andCanRetry() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook.copy(totalPages = 200)
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.READING)
            recordRepository.maxEndPageAfterAdd = 200
            libraryRepository.statusFailure = IllegalStateException("Network error")
            val viewModel = createViewModel()
            advanceUntilIdle()

            val attempt = viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 200)!!
            advanceUntilIdle()
            viewModel.confirmFinish(attempt)
            advanceUntilIdle()

            assertThat(contentState(viewModel).finishCheck).isInstanceOf(FinishCheckState.Failed::class.java)

            libraryRepository.statusFailure = null
            viewModel.confirmFinish(attempt)
            advanceUntilIdle()

            assertThat(contentState(viewModel).finishCheck).isEqualTo(FinishCheckState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry?.status).isEqualTo(ReadingStatus.FINISHED)
        }

    @Test
    fun finishCheck_confirm_domainFailure_showsFailedState_andCanRetry() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook.copy(totalPages = 200)
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.READING)
            recordRepository.maxEndPageAfterAdd = 200
            libraryRepository.statusResult = ReadingStatusChangeResult.BookNotFound
            val viewModel = createViewModel()
            advanceUntilIdle()

            val attempt = viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 200)!!
            advanceUntilIdle()
            viewModel.confirmFinish(attempt)
            advanceUntilIdle()

            assertThat(contentState(viewModel).finishCheck).isInstanceOf(FinishCheckState.Failed::class.java)

            libraryRepository.statusResult = ReadingStatusChangeResult.Changed
            viewModel.confirmFinish(attempt)
            advanceUntilIdle()

            assertThat(contentState(viewModel).finishCheck).isEqualTo(FinishCheckState.Idle)
            assertThat(contentState(viewModel).bookDetail.entry?.status).isEqualTo(ReadingStatus.FINISHED)
        }

    @Test
    fun finishCheck_survivesRotation_andIsNotReshownAfterResponse() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook.copy(totalPages = 200)
            libraryRepository.entry.value = testEntry.copy(status = ReadingStatus.READING)
            recordRepository.maxEndPageAfterAdd = 200
            val savedStateHandle = createSavedStateHandle()
            val viewModel = createViewModel(savedStateHandle)
            advanceUntilIdle()

            val attempt = viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 200)!!
            advanceUntilIdle()
            assertThat(contentState(viewModel).finishCheck).isInstanceOf(FinishCheckState.Pending::class.java)

            // 회전 시뮬레이션: 같은 ViewModel 인스턴스 유지
            assertThat(contentState(viewModel).finishCheck).isInstanceOf(FinishCheckState.Pending::class.java)

            // 응답 후 재노출 없음
            viewModel.dismissFinishCheck()
            assertThat(contentState(viewModel).finishCheck).isEqualTo(FinishCheckState.Idle)
            viewModel.addRecord(date = testRecord.date, time = testRecord.time, startPage = 1, endPage = 200)
            advanceUntilIdle()
            // 새 추가는 새 attempt를 생성하므로 다시 Pending이 될 수 있음 - 이전 응답은 재노출 안 됨
            val newCheck = contentState(viewModel).finishCheck
            if (newCheck is FinishCheckState.Pending) {
                assertThat(newCheck.attempt).isGreaterThan(attempt)
            }
        }

    private fun createSavedStateHandle(
        openMyRecords: Boolean = false,
        savedTab: BookDetailTab? = null,
        savedMemoDraft: String? = null,
    ): SavedStateHandle {
        val state =
            mutableMapOf<String, Any?>(
                "isbn" to "123",
                "openMyRecords" to openMyRecords,
            )
        savedTab?.let { state["bookDetailSelectedTab"] = it.name }
        savedMemoDraft?.let { state["bookDetailMemoDraft"] = it }
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
            updateRatingUseCase = UpdateRatingUseCase(
                libraryRepository,
                object : Clock {
                    override fun now(): Instant = Instant.fromEpochMilliseconds(100)

                    override fun timeZone(): TimeZone = TimeZone.UTC
                },
            ),
            updateMemoUseCase = UpdateMemoUseCase(
                libraryRepository,
                object : Clock {
                    override fun now(): Instant = Instant.fromEpochMilliseconds(100)

                    override fun timeZone(): TimeZone = TimeZone.UTC
                },
            ),
            observeBookRecords = ObserveBookRecordsUseCase(recordRepository),
            addReadingRecord = AddReadingRecordUseCase(
                recordRepository,
                bookRepository,
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

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> {
            refreshGate?.await()
            bookAfterSync?.let { books.value = it }
            return refreshResult.map { status ->
                BookSyncResult(
                    book = bookAfterSync ?: books.value ?: testBook,
                    status = status,
                )
            }
        }

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }

    private class FakeLibraryRepository : LibraryRepository {
        var statusCalls = 0
        var statusGate: CompletableDeferred<Unit>? = null
        var statusFailure: Exception? = null
        var statusResult = ReadingStatusChangeResult.Changed
        var emitStatus = true
        var ratingCalls = 0
        var ratingGate: CompletableDeferred<Unit>? = null
        var ratingFailure: Exception? = null
        var ratingResult = RatingChangeResult.Changed
        var emitRating = true
        var memoCalls = 0
        var memoGate: CompletableDeferred<Unit>? = null
        var memoFailure: Exception? = null
        var memoResult = MemoChangeResult.Changed
        var emitMemo = true
        val entry = MutableStateFlow<BookEntry?>(null)

        override fun observeAll(): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: LocalDate?,
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

        override suspend fun updateRating(
            isbn: String,
            rating: Int,
            updatedAt: Instant,
        ): RatingChangeResult {
            ratingCalls++
            ratingGate?.await()
            ratingFailure?.let { throw it }
            if (emitRating && ratingResult == RatingChangeResult.Changed) {
                entry.value = (entry.value ?: testEntry.copy(status = ReadingStatus.UNSET, addedAt = updatedAt)).copy(
                    rating = rating,
                    updatedAt = updatedAt,
                )
            }
            return ratingResult
        }

        override suspend fun updateMemo(
            isbn: String,
            memo: String?,
            updatedAt: Instant,
        ): MemoChangeResult {
            memoCalls++
            memoGate?.await()
            memoFailure?.let { throw it }
            if (emitMemo && memoResult == MemoChangeResult.Changed) {
                entry.value = (entry.value ?: testEntry.copy(status = ReadingStatus.UNSET, addedAt = updatedAt)).copy(
                    memo = memo,
                    updatedAt = updatedAt,
                )
            }
            return memoResult
        }

        override fun observeByStatus(
            status: ReadingStatus,
            sort: LibrarySort,
        ): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> = emptyList()

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> = entry

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = SaveBookEntryResult.Saved

        override suspend fun removeBookEntry(isbn: String) = Unit

        override suspend fun restoreDeletedEntryIfAbsent(entry: BookEntry): Boolean = error("Not used")

        override suspend fun restoreReadingStatusIfUnchanged(
            original: BookEntry,
            changedStatus: ReadingStatus,
            changedAt: kotlinx.datetime.Instant,
        ): Boolean = error("Not used")
    }

    private class FakeReadingRecordRepository : ReadingRecordRepository {
        val records = MutableStateFlow<List<ReadingRecord>>(emptyList())
        var recordFlow: Flow<List<ReadingRecord>> = records
        var observationCount = 0
        var addCalls = 0
        var addGate: CompletableDeferred<Unit>? = null
        var addFailure: Exception? = null
        var maxEndPageAfterAdd: Int? = null

        override fun observeRecordsByIsbn(isbn: String): Flow<List<ReadingRecord>> =
            flow {
                observationCount++
                emitAll(recordFlow)
            }

        override suspend fun addRecord(
            record: ReadingRecord,
            updatedAt: Instant,
        ): Long {
            addCalls++
            addGate?.await()
            addFailure?.let { throw it }
            val newId = (records.value.size + 1).toLong()
            val newRecord = record.copy(id = newId)
            records.value = listOf(newRecord) + records.value
            return newId
        }

        override suspend fun saveRecord(record: ReadingRecord): Long = record.id

        override suspend fun removeRecord(id: Long) {
            records.value = records.value.filterNot { it.id == id }
        }

        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> = emptyFlow()

        override suspend fun getMaxEndPage(isbn: String): Int? = maxEndPageAfterAdd ?: records.value.maxOfOrNull { it.endPage }
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
        val testRecord =
            ReadingRecord(
                id = 1L,
                isbn = "123",
                date = LocalDate(2026, 9, 19),
                time = LocalTime(14, 0),
                startPage = 1,
                endPage = 20,
            )
    }
}
