package com.leeseungyun1020.manicule.core.domain.scanner

import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.BookSyncResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetBookByScanUseCaseTest {
    @Test
    fun preservesCandidateOrderAndReturnsFirstFoundBooksActualIsbn() =
        runTest {
            val repository = FakeBookRepository(
                books = mapOf("second" to book(isbn = "stored-isbn")),
            )

            val result = GetBookByScanUseCase(repository)(listOf("first", "second", "third"))

            assertThat(result).isEqualTo("stored-isbn")
            assertThat(repository.synced).containsExactly("first")
        }

    @Test
    fun cachedBookSucceedsWithoutSync() =
        runTest {
            val repository = FakeBookRepository(books = mapOf("raw-value" to book("actual-isbn")))

            val result = GetBookByScanUseCase(repository)(listOf("raw-value"))

            assertThat(result).isEqualTo("actual-isbn")
            assertThat(repository.synced).isEmpty()
        }

    @Test
    fun auxiliaryFailureStillReturnsSavedBook() =
        runTest {
            val repository =
                FakeBookRepository(
                    syncResults = mapOf("raw-value" to Result.success(BookSyncStatus.AUXILIARY_CONTENT_FAILED)),
                    syncedBooks = mapOf("raw-value" to book("actual-isbn")),
                )

            val result = GetBookByScanUseCase(repository)(listOf("raw-value"))

            assertThat(result).isEqualTo("actual-isbn")
        }

    @Test
    fun synchronizedBookReturnsItsCanonicalIsbnWithoutRawValueAlias() =
        runTest {
            val repository =
                FakeBookRepository(
                    syncResults = mapOf("raw-value" to Result.success(BookSyncStatus.COMPLETE)),
                    syncedBooks = mapOf("raw-value" to book("canonical-isbn")),
                    doesNotAliasSyncedBooks = true,
                )

            val result = GetBookByScanUseCase(repository)(listOf("raw-value"))

            assertThat(result).isEqualTo("canonical-isbn")
        }

    @Test
    fun allFailuresFinishWithoutWaitingForBook() =
        runTest {
            val repository =
                FakeBookRepository(
                    syncResults = mapOf(
                        "first" to Result.failure(IllegalStateException()),
                        "second" to Result.success(BookSyncStatus.COMPLETE),
                    ),
                )

            val result = GetBookByScanUseCase(repository)(listOf("first", "second"))

            assertThat(result).isNull()
            assertThat(repository.synced).containsExactly("first", "second").inOrder()
        }

    @Test
    fun localLookupFailureContinuesWithNextCandidate() =
        runTest {
            val repository =
                FakeBookRepository(
                    books = mapOf("second" to book("actual-isbn")),
                    localFailures = setOf("first"),
                )

            val result = GetBookByScanUseCase(repository)(listOf("first", "second"))

            assertThat(result).isEqualTo("actual-isbn")
            assertThat(repository.synced).isEmpty()
        }

    @Test
    fun cancellationResultIsPropagated() =
        runTest {
            val repository =
                FakeBookRepository(
                    syncResults = mapOf("raw-value" to Result.failure(CancellationException("cancelled"))),
                )

            var thrown: Throwable? = null
            try {
                GetBookByScanUseCase(repository)(listOf("raw-value"))
            } catch (exception: Throwable) {
                thrown = exception
            }

            assertThat(thrown).isInstanceOf(CancellationException::class.java)
        }

    private class FakeBookRepository(
        private val books: Map<String, Book> = emptyMap(),
        private val syncResults: Map<String, Result<BookSyncStatus>> = emptyMap(),
        private val syncedBooks: Map<String, Book> = emptyMap(),
        private val localFailures: Set<String> = emptySet(),
        private val doesNotAliasSyncedBooks: Boolean = false,
    ) : BookRepository {
        val synced = mutableListOf<String>()
        private val availableBooks = books.toMutableMap()

        override fun observeBook(isbn: String): Flow<Book?> {
            if (isbn in localFailures) throw IllegalStateException(isbn)
            return flowOf(availableBooks[isbn])
        }

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> {
            synced += isbn
            syncedBooks[isbn]?.let { book ->
                if (!doesNotAliasSyncedBooks) availableBooks[isbn] = book
            }
            return syncResults[isbn]
                ?.mapCatching { status ->
                    BookSyncResult(
                        book = syncedBooks[isbn] ?: throw NoSuchElementException(isbn),
                        status = status,
                    )
                }
                ?: Result.failure(NoSuchElementException(isbn))
        }

        override fun searchBooks(query: String): Flow<PagingData<Book>> = error("Not used")
    }

    private fun book(isbn: String) =
        Book(
            isbn = isbn,
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
}
