package com.leeseungyun1020.manicule.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.BookEntryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test

class LibraryRepositoryImplTest {
    private lateinit var bookDataSource: RecordingBookLocalDataSource
    private lateinit var entryDataSource: RecordingBookEntryLocalDataSource
    private lateinit var repository: LibraryRepositoryImpl

    @Before
    fun setup() {
        bookDataSource = RecordingBookLocalDataSource()
        entryDataSource = RecordingBookEntryLocalDataSource()
        repository = LibraryRepositoryImpl(entryDataSource, bookDataSource)
    }

    @Test
    fun saveBookEntry_validRating_savesBookAndEntry() =
        runTest {
            val entry = entry(rating = 5)

            assertThat(repository.saveBookEntry(entry))
                .isEqualTo(SaveBookEntryResult.Saved)
            assertThat(bookDataSource.saved?.isbn).isEqualTo(entry.book.isbn)
            assertThat(entryDataSource.saved?.rating).isEqualTo(5)
        }

    @Test
    fun saveBookEntry_invalidRating_returnsFailureWithoutWriting() =
        runTest {
            listOf(-1, 6).forEach { rating ->
                assertThat(repository.saveBookEntry(entry(rating)))
                    .isEqualTo(SaveBookEntryResult.InvalidRating(rating))
            }

            assertThat(bookDataSource.saved).isNull()
            assertThat(entryDataSource.saved).isNull()
        }

    private fun entry(rating: Int) =
        BookEntry(
            book =
                Book(
                    isbn = "123",
                    title = "Book",
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
                ),
            status = ReadingStatus.UNSET,
            rating = rating,
            addedAt = Instant.fromEpochMilliseconds(0),
            updatedAt = Instant.fromEpochMilliseconds(0),
        )

    @Test
    fun changeStatus_delegatesWithoutRewritingBookOrEntry() =
        runTest {
            val time = Instant.fromEpochMilliseconds(123)
            val date = kotlinx.datetime.LocalDate(2026, 9, 5)
            for (result in ReadingStatusChangeResult.entries) {
                entryDataSource.statusResult = result
                assertThat(repository.changeReadingStatus("123", ReadingStatus.FINISHED, time, date)).isEqualTo(result)
                assertThat(entryDataSource.statusRequest).containsExactly("123", ReadingStatus.FINISHED, time, date).inOrder()
            }
            assertThat(bookDataSource.saved).isNull()
            assertThat(entryDataSource.saved).isNull()
        }

    private class RecordingBookLocalDataSource : BookLocalDataSource {
        var saved: BookEntity? = null

        override suspend fun getByIsbn(isbn: String): BookEntity? = null

        override fun observeByIsbn(isbn: String): Flow<BookEntity?> = emptyFlow()

        override suspend fun save(book: BookEntity) {
            saved = book
        }
    }

    private class RecordingBookEntryLocalDataSource : BookEntryLocalDataSource {
        var statusResult = ReadingStatusChangeResult.Changed
        var statusRequest: List<Any?> = emptyList()

        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: kotlinx.datetime.LocalDate?,
        ): ReadingStatusChangeResult {
            statusRequest = listOf(isbn, status, updatedAt, finishedAt)
            return statusResult
        }

        var saved: BookEntryEntity? = null

        override suspend fun save(entry: BookEntryEntity) {
            saved = entry
        }

        override suspend fun remove(isbn: String) = Unit

        override fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?> = emptyFlow()

        override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>> = emptyFlow()

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<BookEntity> = emptyList()

        override fun observeAll(): Flow<List<BookEntryWithCurrentPage>> = emptyFlow()
    }
}
