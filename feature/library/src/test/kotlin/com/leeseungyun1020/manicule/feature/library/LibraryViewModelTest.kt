package com.leeseungyun1020.manicule.feature.library

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class LibraryViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = ControllableLibraryRepository()

    @Test
    fun initialStatus_isReading_andEmitsContent() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = LibraryViewModel(GetLibraryBooksUseCase(repository))
            viewModel.uiState.test {
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.READING))
                repository.flow(ReadingStatus.READING).emit(emptyList())
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Content(ReadingStatus.READING, emptyList()))
            }
        }

    @Test
    fun selectingTab_cancelsPreviousSubscription() =
        runTest(mainDispatcherRule.dispatcher) {
            val viewModel = LibraryViewModel(GetLibraryBooksUseCase(repository))
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
    fun repositoryFailure_emitsError_andRetrySubscribesAgain() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.fail = true
            val viewModel = LibraryViewModel(GetLibraryBooksUseCase(repository))
            viewModel.uiState.test {
                awaitItem()
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Error(ReadingStatus.READING))
                repository.fail = false
                viewModel.retry()
                assertThat(awaitItem()).isEqualTo(LibraryUiState.Loading(ReadingStatus.READING))
                assertThat(repository.subscriptionCount).isEqualTo(2)
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private class ControllableLibraryRepository : LibraryRepository {
    private val flows = ReadingStatus.entries.associateWith { MutableSharedFlow<List<BookEntry>>(replay = 1) }
    var lastStatus: ReadingStatus? = null
    var subscriptionCount = 0
    var fail = false

    fun flow(status: ReadingStatus): MutableSharedFlow<List<BookEntry>> = checkNotNull(flows[status])

    override fun observeAll(): Flow<List<BookEntry>> = flow(ReadingStatus.READING)

    override suspend fun changeReadingStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: kotlinx.datetime.Instant,
        finishedAt: kotlinx.datetime.LocalDate?,
    ): ReadingStatusChangeResult = error("Not used by this test")

    override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> =
        flow {
            lastStatus = status
            subscriptionCount += 1
            if (fail) throw IOException("failed")
            flow(status).collect(::emit)
        }

    override fun observeBookEntry(isbn: String): Flow<BookEntry?> = flow { emit(null) }

    override suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book> = emptyList()

    override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = SaveBookEntryResult.Saved

    override suspend fun removeBookEntry(isbn: String) = Unit
}
