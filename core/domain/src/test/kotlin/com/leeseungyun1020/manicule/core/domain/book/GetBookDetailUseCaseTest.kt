package com.leeseungyun1020.manicule.core.domain.book

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookDetail
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Test

class GetBookDetailUseCaseTest {
    private val bookRepository = FakeBookRepository()
    private val libraryRepository = FakeLibraryRepository()
    private val useCase = GetBookDetailUseCase(bookRepository, libraryRepository)

    @Test
    fun invoke_combinesCachedBookAndOptionalEntry() =
        runTest {
            useCase("123").test {
                assertThat(awaitItem()).isNull()
                bookRepository.books.value = testBook
                assertThat(awaitItem()).isEqualTo(BookDetail(testBook, entry = null))
                libraryRepository.entry.value = testEntry
                assertThat(awaitItem()).isEqualTo(BookDetail(testBook, entry = testEntry))
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun refresh_delegatesToRepository() =
        runTest {
            bookRepository.refreshResult = Result.failure(IllegalStateException("failed"))

            val result = useCase.refresh("123")

            assertThat(result.isFailure).isTrue()
            assertThat(bookRepository.refreshedIsbn).isEqualTo("123")
        }

    private class FakeBookRepository : BookRepository {
        val books = MutableStateFlow<Book?>(null)
        var refreshResult: Result<BookSyncStatus> = Result.success(BookSyncStatus.COMPLETE)
        var refreshedIsbn: String? = null

        override fun observeBook(isbn: String): Flow<Book?> = books

        override suspend fun syncBook(isbn: String): Result<BookSyncStatus> {
            refreshedIsbn = isbn
            return refreshResult
        }

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }

    private class FakeLibraryRepository : LibraryRepository {
        val entry = MutableStateFlow<BookEntry?>(null)

        override fun observeAll(): Flow<List<BookEntry>> = emptyFlow()

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
                status = ReadingStatus.UNSET,
                rating = 4,
                memo = "Review only",
                addedAt = Instant.fromEpochMilliseconds(1),
                updatedAt = Instant.fromEpochMilliseconds(1),
            )
    }
}
