package com.leeseungyun1020.manicule.feature.library

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
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
    fun repositoryFailure_emitsError_andRetrySubscribesAgain() =
        runTest(mainDispatcherRule.dispatcher) {
            repository.fail = true
            val viewModel = createViewModel()
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

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()): LibraryViewModel =
        LibraryViewModel(GetLibraryBooksUseCase(repository), savedStateHandle)
}

private class ControllableLibraryRepository : LibraryRepository {
    private val flows = ReadingStatus.entries.associateWith { MutableSharedFlow<List<BookEntry>>(replay = 1) }
    var lastStatus: ReadingStatus? = null
    var subscriptionCount = 0
    var fail = false

    fun flow(status: ReadingStatus): MutableSharedFlow<List<BookEntry>> = checkNotNull(flows[status])

    override fun observeAll(): Flow<List<BookEntry>> = flow(ReadingStatus.READING)

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
