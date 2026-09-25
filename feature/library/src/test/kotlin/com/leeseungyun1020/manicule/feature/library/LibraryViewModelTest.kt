package com.leeseungyun1020.manicule.feature.library

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.SystemClock
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.domain.library.ChangeReadingStatusUseCase
import com.leeseungyun1020.manicule.core.domain.library.DeleteBookEntryUseCase
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.domain.library.RestoreBookEntryUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryTab
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.IOException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class LibraryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = ControllableLibraryRepository()

    @Test
    fun initialStatus_isReading_andEmitsContent() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.READING))
                repository.flow(ReadingStatus.READING).emit(emptyList())
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Content(ReadingStatus.READING, emptyList()))
                assertThat(repository.lastSort).isEqualTo(LibrarySort.Default)
            }
        }

    @Test
    fun selectingSort_reloadsWithSelectedSort() =
        runTest(mainDispatcherRule.dispatcher) {
            val sort =
                LibrarySort(
                    criterion = LibrarySort.Criterion.RATING,
                    direction = LibrarySort.Direction.ASCENDING,
                )
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                repository.flow(ReadingStatus.READING).emit(emptyList())
                awaitItem()

                viewModel.selectSort(sort)

                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.READING, sort))
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Content(ReadingStatus.READING, emptyList(), sort))
                assertThat(repository.lastSort).isEqualTo(sort)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun selectingTab_cancelsPreviousSubscription() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                viewModel.selectStatus(ReadingStatus.WANT)
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.WANT))
                assertThat(repository.lastStatus).isEqualTo(ReadingStatus.WANT)
                repository.flow(ReadingStatus.READING).emit(emptyList())
                expectNoEvents()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun selectingTab_keepsSelectedSort() =
        runTest(mainDispatcherRule.dispatcher) {
            val sort =
                LibrarySort(
                    criterion = LibrarySort.Criterion.ADDED_AT,
                    direction = LibrarySort.Direction.ASCENDING,
                )
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                viewModel.selectSort(sort)
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.READING, sort))

                viewModel.selectStatus(ReadingStatus.FINISHED)

                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.FINISHED, sort))
                assertThat(repository.lastSort).isEqualTo(sort)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun repositoryFailure_emitsError_andRetrySubscribesAgain() =
        runTest(mainDispatcherRule.dispatcher) {
            val sort =
                LibrarySort(
                    criterion = LibrarySort.Criterion.RATING,
                    direction = LibrarySort.Direction.DESCENDING,
                )
            repository.fail = true
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Error(ReadingStatus.READING))
                viewModel.selectSort(sort)
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.READING, sort))
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Error(ReadingStatus.READING, sort))
                repository.fail = false
                viewModel.retry()
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.READING, sort))
                assertThat(repository.lastSort).isEqualTo(sort)
                assertThat(repository.subscriptionCount).isEqualTo(3)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun initialWantTab_isUsedForLoadingAndRepositoryQuery() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel(SavedStateHandle(mapOf("initialTab" to LibraryTab.WANT)))
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.WANT))
                repository.flow(ReadingStatus.WANT).emit(emptyList())
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Content(ReadingStatus.WANT, emptyList()))
                assertThat(repository.lastStatus).isEqualTo(ReadingStatus.WANT)
            }
        }

    @Test
    fun initialFinishedTab_isUsedForLoadingAndRepositoryQuery() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = createViewModel(SavedStateHandle(mapOf("initialTab" to LibraryTab.FINISHED)))
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.FINISHED))
                repository.flow(ReadingStatus.FINISHED).emit(emptyList())
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Content(ReadingStatus.FINISHED, emptyList()))
            }
        }

    @Test
    fun retry_keepsRequestedWantTab() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.fail = true
            val viewModel = createViewModel(SavedStateHandle(mapOf("initialTab" to LibraryTab.WANT)))
            viewModel.uiState.test {
                awaitItem()
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Error(ReadingStatus.WANT))
                repository.fail = false
                viewModel.retry()
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.WANT))
                repository.flow(ReadingStatus.WANT).emit(emptyList())
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Content(ReadingStatus.WANT, emptyList()))
                assertThat(repository.subscriptionCount).isEqualTo(2)
            }
        }

    @Test
    fun restoredUserSelection_takesPrecedenceOverInitialTab() =
        runTest(mainDispatcherRule.dispatcher) {
            val savedStateHandle = SavedStateHandle(mapOf("initialTab" to LibraryTab.WANT))
            val viewModel = createViewModel(savedStateHandle)
            viewModel.selectStatus(ReadingStatus.FINISHED)

            val restoredHandle = SavedStateHandle(savedStateHandle.keys().associateWith { savedStateHandle.get<Any>(it) })
            val restoredViewModel = createViewModel(restoredHandle)
            restoredViewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.FINISHED))
                repository.flow(ReadingStatus.FINISHED).emit(emptyList())
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Content(ReadingStatus.FINISHED, emptyList()))
            }
        }

    @Test
    fun unknownSavedTab_fallsBackToRouteTab() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel =
                createViewModel(
                    SavedStateHandle(mapOf("initialTab" to LibraryTab.WANT, "librarySelectedTab" to "unknown")),
                )

            assertThat(viewModel.uiState.value).isEqualTo(LibraryUiState.Loading(ReadingStatus.WANT))
        }

    @Test
    fun unsetStatus_doesNotReplaceVisibleTab() =
        runTest(mainDispatcherRule.dispatcher) {
            val savedStateHandle = SavedStateHandle(mapOf("initialTab" to LibraryTab.WANT))
            val viewModel = createViewModel(savedStateHandle)
            viewModel.uiState.test {
                awaitItem()
                repository.flow(ReadingStatus.WANT).emit(emptyList())
                awaitItem()
                viewModel.selectStatus(ReadingStatus.UNSET)
                expectNoEvents()
                assertThat(savedStateHandle.contains("librarySelectedTab")).isFalse()
            }
        }

    @Test
    fun changedStatus_canUndoCompleteEntrySnapshot() =
        runTest(mainDispatcherRule.dispatcher) {
            val entry = testEntry()
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                repository.flow(ReadingStatus.READING).emit(listOf(entry))
                awaitItem()
                viewModel.changeStatus(entry.book.isbn, ReadingStatus.FINISHED)
                advanceUntilIdle()
                val message = checkNotNull(viewModel.actionMessage.value)
                assertThat(message.kind).isEqualTo(LibraryActionMessageKind.STATUS_CHANGED)
                assertThat(repository.changedStatus).isEqualTo(ReadingStatus.FINISHED)
                viewModel.undo(message.id)
                advanceUntilIdle()
                assertThat(repository.savedEntry).isEqualTo(entry)
                assertThat(viewModel.actionMessage.value).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun deleteFailure_doesNotOfferUndo() =
        runTest(mainDispatcherRule.dispatcher) {
            val entry = testEntry()
            repository.deleteFailure = IOException("delete failed")
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                repository.flow(ReadingStatus.READING).emit(listOf(entry))
                awaitItem()
                viewModel.deleteBook(entry.book.isbn)
                advanceUntilIdle()
                val message = checkNotNull(viewModel.actionMessage.value)
                assertThat(message.kind).isEqualTo(LibraryActionMessageKind.ACTION_FAILED)
                viewModel.undo(message.id)
                advanceUntilIdle()
                assertThat(repository.savedEntry).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun undoFailure_retainsSnapshotForRetry() =
        runTest(mainDispatcherRule.dispatcher) {
            val entry = testEntry()
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                repository.flow(ReadingStatus.READING).emit(listOf(entry))
                awaitItem()
                viewModel.deleteBook(entry.book.isbn)
                advanceUntilIdle()
                val id = checkNotNull(viewModel.actionMessage.value).id
                repository.saveFailure = IOException("restore failed")
                viewModel.undo(id)
                advanceUntilIdle()
                val firstFailure = checkNotNull(viewModel.actionMessage.value)
                assertThat(firstFailure.kind).isEqualTo(LibraryActionMessageKind.UNDO_FAILED)
                viewModel.undo(id)
                advanceUntilIdle()
                val secondFailure = checkNotNull(viewModel.actionMessage.value)
                assertThat(secondFailure.kind).isEqualTo(LibraryActionMessageKind.UNDO_FAILED)
                assertThat(secondFailure.revision).isGreaterThan(firstFailure.revision)
                viewModel.messageDismissed(id)
                val redisplayed = checkNotNull(viewModel.actionMessage.value)
                assertThat(redisplayed.revision).isGreaterThan(secondFailure.revision)
                repository.saveFailure = null
                viewModel.undo(id)
                advanceUntilIdle()
                assertThat(repository.savedEntry).isEqualTo(entry)
                assertThat(viewModel.actionMessage.value).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun nextAction_invalidatesPreviousUndoId() =
        runTest(mainDispatcherRule.dispatcher) {
            val first = testEntry()
            val second = testEntry().copy(book = testEntry().book.copy(isbn = "9780000000002"))
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                repository.flow(ReadingStatus.READING).emit(listOf(first, second))
                awaitItem()
                viewModel.deleteBook(first.book.isbn)
                advanceUntilIdle()
                val oldId = checkNotNull(viewModel.actionMessage.value).id
                viewModel.deleteBook(second.book.isbn)
                advanceUntilIdle()
                val newId = checkNotNull(viewModel.actionMessage.value).id
                assertThat(newId).isNotEqualTo(oldId)
                viewModel.undo(oldId)
                advanceUntilIdle()
                assertThat(repository.savedEntry).isNull()
                viewModel.undo(newId)
                advanceUntilIdle()
                assertThat(repository.savedEntry).isEqualTo(second)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun olderRestoreCompletion_keepsNewerUndoAvailable() =
        runTest(mainDispatcherRule.dispatcher) {
            val first = testEntry()
            val second = first.copy(book = first.book.copy(isbn = "9780000000002"))
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                repository.flow(ReadingStatus.READING).emit(listOf(first, second))
                awaitItem()
                viewModel.deleteBook(first.book.isbn)
                advanceUntilIdle()
                val oldId = checkNotNull(viewModel.actionMessage.value).id

                val restoreGate = CompletableDeferred<Unit>()
                repository.saveGate = restoreGate
                viewModel.undo(oldId)
                advanceUntilIdle()

                viewModel.deleteBook(second.book.isbn)
                advanceUntilIdle()
                val newId = checkNotNull(viewModel.actionMessage.value).id
                assertThat(newId).isNotEqualTo(oldId)

                repository.saveGate = null
                restoreGate.complete(Unit)
                advanceUntilIdle()
                assertThat(viewModel.actionMessage.value?.id).isEqualTo(newId)

                viewModel.undo(newId)
                advanceUntilIdle()
                assertThat(repository.savedEntry).isEqualTo(second)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun unchangedStatus_doesNotCreateUndo() =
        runTest(mainDispatcherRule.dispatcher) {
            val entry = testEntry()
            repository.statusResult = ReadingStatusChangeResult.Unchanged
            val viewModel = createViewModel()
            viewModel.uiState.test {
                awaitItem()
                repository.flow(ReadingStatus.READING).emit(listOf(entry))
                awaitItem()
                viewModel.changeStatus(entry.book.isbn, ReadingStatus.WANT)
                advanceUntilIdle()
                val message = checkNotNull(viewModel.actionMessage.value)
                assertThat(message.kind).isEqualTo(LibraryActionMessageKind.ACTION_FAILED)
                viewModel.undo(message.id)
                advanceUntilIdle()
                assertThat(repository.savedEntry).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun testEntry(): BookEntry =
        BookEntry(
            book = Book(
                isbn = "9780000000001",
                title = "Test",
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
            ),
            status = ReadingStatus.READING,
            rating = 4,
            memo = "Review",
            addedAt = Instant.fromEpochMilliseconds(1),
            updatedAt = Instant.fromEpochMilliseconds(2),
        )

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): LibraryViewModel =
        LibraryViewModel(
            GetLibraryBooksUseCase(repository),
            ChangeReadingStatusUseCase(repository, SystemClock()),
            DeleteBookEntryUseCase(repository),
            RestoreBookEntryUseCase(repository),
            savedStateHandle,
        )
}

private class ControllableLibraryRepository : LibraryRepository {
    private val flows = ReadingStatus.entries.associateWith { MutableSharedFlow<List<BookEntry>>(replay = 1) }
    var lastStatus: ReadingStatus? = null
    var lastSort: LibrarySort? = null
    var subscriptionCount = 0
    var fail = false
    var changedStatus: ReadingStatus? = null
    var statusResult = ReadingStatusChangeResult.Changed
    var deleteFailure: Exception? = null
    var saveFailure: Exception? = null
    var saveGate: CompletableDeferred<Unit>? = null
    var savedEntry: BookEntry? = null

    fun flow(status: ReadingStatus): MutableSharedFlow<List<BookEntry>> = checkNotNull(flows[status])

    override fun observeAll(): Flow<List<BookEntry>> = flow(ReadingStatus.READING)

    override suspend fun changeReadingStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: kotlinx.datetime.Instant,
        finishedAt: kotlinx.datetime.LocalDate?,
    ): ReadingStatusChangeResult {
        changedStatus = status
        return statusResult
    }

    override fun observeByStatus(
        status: ReadingStatus,
        sort: LibrarySort,
    ): Flow<List<BookEntry>> =
        flow {
            lastStatus = status
            lastSort = sort
            subscriptionCount += 1
            if (fail) throw IOException("failed")
            flow(status).collect(::emit)
        }

    override fun observeBookEntry(isbn: String): Flow<BookEntry?> = flow { emit(null) }

    override suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book> = emptyList()

    override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult {
        saveFailure?.let { throw it }
        saveGate?.await()
        savedEntry = entry
        return SaveBookEntryResult.Saved
    }

    override suspend fun removeBookEntry(isbn: String) {
        deleteFailure?.let { throw it }
    }
}
