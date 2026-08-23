package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.domain.book.GetBookDetailUseCase
import com.leeseungyun1020.manicule.core.domain.library.ObserveBookEntryUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.Instant
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

            assertThat(viewModel.uiState.value.book).isEqualTo(testBook)
            assertThat(viewModel.uiState.value.isFatalError).isFalse()
            assertThat(viewModel.uiState.value.isRefreshError).isTrue()
        }

    @Test
    fun missingBookAndRefreshFailure_isFatalError() =
        runTest(dispatcher) {
            bookRepository.refreshResult = Result.failure(NoSuchElementException())

            val viewModel = createViewModel()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.isFatalError).isTrue()
        }

    @Test
    fun retry_clearsPreviousErrors_andUpdatesStateOnSuccess() =
        runTest(dispatcher) {
            bookRepository.refreshResult = Result.failure(NoSuchElementException())
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.isFatalError).isTrue()

            bookRepository.refreshResult = Result.success(Unit)
            bookRepository.books.value = testBook
            viewModel.retry()
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.book).isEqualTo(testBook)
            assertThat(viewModel.uiState.value.isFatalError).isFalse()
            assertThat(viewModel.uiState.value.isRefreshError).isFalse()
        }

    @Test
    fun libraryEntry_selectsRecordsInitially_butDoesNotOverrideUserSelection() =
        runTest(dispatcher) {
            bookRepository.books.value = testBook
            libraryRepository.entry.value = testEntry
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertThat(viewModel.uiState.value.selectedTab).isEqualTo(BookDetailTab.MyRecords)

            viewModel.selectTab(BookDetailTab.Information)
            libraryRepository.entry.value = null
            libraryRepository.entry.value = testEntry
            advanceUntilIdle()

            assertThat(viewModel.uiState.value.selectedTab).isEqualTo(BookDetailTab.Information)
        }

    @Test
    fun explicitAndRestoredTab_areApplied() =
        runTest(dispatcher) {
            val explicit = createViewModel(openMyRecords = true)
            advanceUntilIdle()
            assertThat(explicit.uiState.value.selectedTab).isEqualTo(BookDetailTab.MyRecords)

            val restored = createViewModel(savedTab = BookDetailTab.Information)
            advanceUntilIdle()
            assertThat(restored.uiState.value.selectedTab).isEqualTo(BookDetailTab.Information)
        }

    private fun createViewModel(
        openMyRecords: Boolean = false,
        savedTab: BookDetailTab? = null,
    ): BookDetailViewModel {
        val state =
            mutableMapOf<String, Any?>(
                "isbn" to "123",
                "openMyRecords" to openMyRecords,
            )
        savedTab?.let { state["bookDetailSelectedTab"] = it.name }
        return BookDetailViewModel(
            getBookDetail = GetBookDetailUseCase(bookRepository),
            observeBookEntry = ObserveBookEntryUseCase(libraryRepository),
            savedStateHandle = SavedStateHandle(state),
        )
    }

    private class FakeBookRepository : BookRepository {
        val books = MutableStateFlow<Book?>(null)
        var refreshResult: Result<Unit> = Result.success(Unit)

        override fun observeBook(isbn: String): Flow<Book?> = books

        override suspend fun syncBook(isbn: String): Result<Unit> = refreshResult

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }

    private class FakeLibraryRepository : LibraryRepository {
        val entry = MutableStateFlow<BookEntry?>(null)

        override fun observeAll(): Flow<List<BookEntry>> = emptyFlow()

        override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> = emptyFlow()

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> = entry

        override suspend fun saveBookEntry(entry: BookEntry) = Unit

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
