package com.leeseungyun1020.manicule.core.domain.stats

import androidx.paging.PagingData
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.BookSyncResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Test

class GetReadingDayBooksUseCaseTest {
    private val date = LocalDate(2026, 9, 24)

    @Test
    fun groups_sessions_and_sorts_by_last_time_then_isbn() =
        runTest {
            val records = FakeStatsRepository()
            val books = FakeBooks()
            records.records.value = listOf(
                record(1, "b", 9, 1, 10),
                record(2, "b", 10, 1, 10),
                record(3, "c", 10, 3, 5),
                record(4, "a", 8, 1, 7),
            )

            val result = GetReadingDayBooksUseCase(records, books)(date).first()

            assertThat(result.map { it.isbn }).containsExactly("b", "c", "a").inOrder()
            assertThat(result.first().recordCount).isEqualTo(2)
            assertThat(result.first().pagesRead).isEqualTo(20)
            assertThat(result.first().book).isNull()
            assertThat(books.observed).containsExactly("b", "c", "a")
        }

    @Test
    fun empty_date_does_not_subscribe_to_books() =
        runTest {
            val books = FakeBooks()

            assertThat(GetReadingDayBooksUseCase(FakeStatsRepository(), books)(date).first()).isEmpty()
            assertThat(books.observed).isEmpty()
        }

    @Test
    fun book_cache_and_record_changes_update_rows() =
        runTest {
            val records = FakeStatsRepository()
            val books = FakeBooks()
            records.records.value = listOf(record(1, "a", 9, 1, 10))

            GetReadingDayBooksUseCase(records, books)(date).test {
                assertThat(awaitItem().single().book).isNull()
                books.books.getValue("a").value = book("a", "Updated title")
                assertThat(awaitItem().single().book?.title).isEqualTo("Updated title")
                records.records.value = emptyList()
                assertThat(awaitItem()).isEmpty()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun record_query_error_propagates() =
        runTest {
            val failure = IllegalStateException("database unavailable")
            val records = object : com.leeseungyun1020.manicule.core.data.repository.StatsRepository by FakeStatsRepository() {
                override fun observeRecordsBetween(
                    start: LocalDate,
                    end: LocalDate,
                ): Flow<List<ReadingRecord>> = flow { throw failure }
            }

            val result = runCatching { GetReadingDayBooksUseCase(records, FakeBooks())(date).first() }

            assertThat(result.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
            assertThat(result.exceptionOrNull()).hasMessageThat().contains("database unavailable")
        }

    private fun record(
        id: Long,
        isbn: String,
        hour: Int,
        start: Int,
        end: Int,
    ) = ReadingRecord(id, isbn, date, LocalTime(hour, 0), start, end)

    private fun book(
        isbn: String,
        title: String,
    ) = Book(
        isbn = isbn,
        title = title,
        author = "",
        publisher = "",
        publishedDate = null,
        coverUrl = null,
        totalPages = null,
        price = null,
        category = null,
        tableOfContentsUrl = null,
        introductionUrl = null,
        summaryUrl = null,
    )

    private class FakeBooks : BookRepository {
        val observed = mutableListOf<String>()
        val books = mutableMapOf<String, MutableStateFlow<Book?>>()

        override fun observeBook(isbn: String): Flow<Book?> {
            observed += isbn
            return books.getOrPut(isbn) { MutableStateFlow(null) }
        }

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> = error("Unexpected sync")

        override fun searchBooks(query: String): Flow<PagingData<Book>> = flowOf(PagingData.empty())
    }
}
