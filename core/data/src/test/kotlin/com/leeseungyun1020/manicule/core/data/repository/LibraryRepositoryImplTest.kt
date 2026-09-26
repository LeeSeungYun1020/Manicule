package com.leeseungyun1020.manicule.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.BookEntryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.MemoChangeResult
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
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

    @Test
    fun undoOperations_doNotRewriteBookMetadata() =
        runTest {
            val original = entry(rating = 4)
            val changedAt = Instant.fromEpochMilliseconds(3)

            assertThat(repository.restoreDeletedEntryIfAbsent(original)).isTrue()
            assertThat(entryDataSource.saved).isEqualTo(original.asEntity())
            assertThat(repository.restoreReadingStatusIfUnchanged(original, ReadingStatus.FINISHED, changedAt)).isTrue()
            assertThat(entryDataSource.restoredStatusRequest)
                .containsExactly(
                    original.book.isbn,
                    ReadingStatus.FINISHED,
                    changedAt,
                    original.status,
                    original.updatedAt,
                    original.finishedAt,
                )
                .inOrder()
            assertThat(bookDataSource.saved).isNull()
        }

    @Test
    fun observeByStatus_forwardsSort() {
        val sort =
            LibrarySort(
                criterion = LibrarySort.Criterion.RATING,
                direction = LibrarySort.Direction.ASCENDING,
            )

        repository.observeByStatus(ReadingStatus.FINISHED, sort)

        assertThat(entryDataSource.observedStatus).isEqualTo(ReadingStatus.FINISHED)
        assertThat(entryDataSource.observedSort).isEqualTo(sort)
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
            val date = LocalDate(2026, 9, 5)
            for (result in ReadingStatusChangeResult.entries) {
                entryDataSource.statusResult = result
                assertThat(repository.changeReadingStatus("123", ReadingStatus.FINISHED, time, date)).isEqualTo(result)
                assertThat(entryDataSource.statusRequest).containsExactly("123", ReadingStatus.FINISHED, time, date).inOrder()
            }
            assertThat(bookDataSource.saved).isNull()
            assertThat(entryDataSource.saved).isNull()
        }

    @Test
    fun updateRating_delegatesWithoutRewritingBookOrEntry() =
        runTest {
            val time = Instant.fromEpochMilliseconds(123)
            for (result in RatingChangeResult.entries) {
                entryDataSource.ratingResult = result
                assertThat(repository.updateRating("123", 4, time)).isEqualTo(result)
                assertThat(entryDataSource.ratingRequest).containsExactly("123", 4, time).inOrder()
            }
            assertThat(bookDataSource.saved).isNull()
            assertThat(entryDataSource.saved).isNull()
        }

    @Test
    fun updateMemo_delegatesWithoutRewritingBookOrEntry() =
        runTest {
            val time = Instant.fromEpochMilliseconds(123)
            for (result in MemoChangeResult.entries) {
                entryDataSource.memoResult = result
                assertThat(repository.updateMemo("123", "새 메모", time)).isEqualTo(result)
                assertThat(entryDataSource.memoRequest).containsExactly("123", "새 메모", time).inOrder()
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
            finishedAt: LocalDate?,
        ): ReadingStatusChangeResult {
            statusRequest = listOf(isbn, status, updatedAt, finishedAt)
            return statusResult
        }

        var ratingResult = RatingChangeResult.Changed
        var ratingRequest: List<Any?> = emptyList()

        override suspend fun updateRating(
            isbn: String,
            rating: Int,
            updatedAt: Instant,
        ): RatingChangeResult {
            ratingRequest = listOf(isbn, rating, updatedAt)
            return ratingResult
        }

        var memoResult = MemoChangeResult.Changed
        var memoRequest: List<Any?> = emptyList()

        override suspend fun updateMemo(
            isbn: String,
            memo: String?,
            updatedAt: Instant,
        ): MemoChangeResult {
            memoRequest = listOf(isbn, memo, updatedAt)
            return memoResult
        }

        var saved: BookEntryEntity? = null
        var observedStatus: ReadingStatus? = null
        var observedSort: LibrarySort? = null
        var restoredStatusRequest: List<Any?> = emptyList()

        override suspend fun save(entry: BookEntryEntity) {
            saved = entry
        }

        override suspend fun insertIfAbsent(entry: BookEntryEntity): Boolean {
            saved = entry
            return true
        }

        override suspend fun restoreStatusIfUnchanged(
            isbn: String,
            changedStatus: ReadingStatus,
            changedAt: Instant,
            originalStatus: ReadingStatus,
            originalUpdatedAt: Instant,
            originalFinishedAt: kotlinx.datetime.LocalDate?,
        ): Boolean {
            restoredStatusRequest = listOf(isbn, changedStatus, changedAt, originalStatus, originalUpdatedAt, originalFinishedAt)
            return true
        }

        override suspend fun remove(isbn: String) = Unit

        override fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?> = emptyFlow()

        override fun observeByStatus(
            status: ReadingStatus,
            sort: LibrarySort,
        ): Flow<List<BookEntryWithCurrentPage>> {
            observedStatus = status
            observedSort = sort
            return emptyFlow()
        }

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<BookEntity> = emptyList()

        override fun observeAll(): Flow<List<BookEntryWithCurrentPage>> = emptyFlow()
    }
}
