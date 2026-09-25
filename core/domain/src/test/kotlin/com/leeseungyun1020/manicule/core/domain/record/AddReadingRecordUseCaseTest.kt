package com.leeseungyun1020.manicule.core.domain.record

import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.BookSyncResult
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Test

class AddReadingRecordUseCaseTest {
    private val recordRepo = RecordRepository()
    private val bookRepo = BookRepository()
    private val libraryRepo = LibraryRepository()
    private val now = Instant.parse("2026-09-16T12:00:00Z")
    private var clockReads = 0
    private val clock =
        object : Clock {
            override fun now(): Instant {
                clockReads++
                return now
            }

            override fun timeZone(): TimeZone = TimeZone.UTC
        }
    private val useCase = AddReadingRecordUseCase(recordRepo, bookRepo, libraryRepo, clock)
    private val date = LocalDate(2026, 9, 15)
    private val time = LocalTime(21, 30)

    @Test
    fun addsNewSessionWithSelectedDateTimeAndCurrentModificationTime() =
        runTest {
            val result = useCase("123", date, time, 11, 42)
            assertThat(result.recordId).isEqualTo(7L)
            assertThat(recordRepo.request).isEqualTo(ReadingRecord(0, "123", date, time, 11, 42))
            assertThat(recordRepo.request?.pagesRead).isEqualTo(32)
            assertThat(recordRepo.updatedAt).isEqualTo(now)
            assertThat(clockReads).isEqualTo(1)
        }

    @Test
    fun invalidInputDoesNotReadClockOrWriteStorage() =
        runTest {
            listOf(Triple(" ", 1, 10), Triple("123", 0, 10), Triple("123", 10, 9)).forEach { (isbn, start, end) ->
                try {
                    useCase(isbn, date, time, start, end)
                    error("Expected invalid input")
                } catch (_: IllegalArgumentException) {
                    assertThat(recordRepo.request).isNull()
                    assertThat(clockReads).isEqualTo(0)
                }
            }
        }

    @Test
    fun storageFailureAndCancellationPropagateWithoutRetry() =
        runTest {
            listOf(IllegalStateException("Storage failure"), CancellationException("Cancelled")).forEach { failure ->
                recordRepo.failure = failure
                try {
                    useCase("123", date, time, 1, 10)
                    error("Expected failure")
                } catch (actual: Exception) {
                    assertThat(actual).isSameInstanceAs(failure)
                }
            }
            assertThat(recordRepo.calls).isEqualTo(2)
        }

    // 다 읽음 판정: T=1000, P=900 → 잔여 100 (T×10%=100) → 경계: 충족
    @Test
    fun finishCheck_atTenPercentBoundary_isTrue() =
        runTest {
            bookRepo.book.value = book(totalPages = 1000)
            recordRepo.maxEndPage = 900
            val result = useCase("123", date, time, 1, 10)
            assertThat(result.shouldCheckFinish).isTrue()
        }

    // 다 읽음 판정: T=1000, P=899 → 잔여 101 > 100 → 미충족
    @Test
    fun finishCheck_beyondTenPercentBoundary_isFalse() =
        runTest {
            bookRepo.book.value = book(totalPages = 1000)
            recordRepo.maxEndPage = 899
            val result = useCase("123", date, time, 1, 10)
            assertThat(result.shouldCheckFinish).isFalse()
        }

    // 다 읽음 판정: T=200, P=160 → 잔여 40 (≤40) → 충족
    @Test
    fun finishCheck_atFortyPageBoundary_isTrue() =
        runTest {
            bookRepo.book.value = book(totalPages = 200)
            recordRepo.maxEndPage = 160
            val result = useCase("123", date, time, 1, 10)
            assertThat(result.shouldCheckFinish).isTrue()
        }

    // 다 읽음 판정: T=200, P=159 → 잔여 41 > max(20, 40) → 미충족
    @Test
    fun finishCheck_beyondFortyPageBoundary_isFalse() =
        runTest {
            bookRepo.book.value = book(totalPages = 200)
            recordRepo.maxEndPage = 159
            val result = useCase("123", date, time, 1, 10)
            assertThat(result.shouldCheckFinish).isFalse()
        }

    // 쪽수 미상이면 판정 생략
    @Test
    fun finishCheck_unknownTotalPages_isFalse() =
        runTest {
            bookRepo.book.value = book(totalPages = null)
            recordRepo.maxEndPage = 300
            val result = useCase("123", date, time, 1, 10)
            assertThat(result.shouldCheckFinish).isFalse()
        }

    // 전체 쪽수 초과도 조건 충족으로 봄
    @Test
    fun finishCheck_endPageExceedsTotalPages_isTrue() =
        runTest {
            bookRepo.book.value = book(totalPages = 200)
            recordRepo.maxEndPage = 210
            val result = useCase("123", date, time, 1, 10)
            assertThat(result.shouldCheckFinish).isTrue()
        }

    // 이미 FINISHED 상태면 재질문하지 않음
    @Test
    fun finishCheck_alreadyFinished_isFalse() =
        runTest {
            bookRepo.book.value = book(totalPages = 200)
            recordRepo.maxEndPage = 200
            libraryRepo.status = ReadingStatus.FINISHED
            val result = useCase("123", date, time, 1, 10)
            assertThat(result.shouldCheckFinish).isFalse()
        }

    private fun book(totalPages: Int?) =
        Book(
            isbn = "123",
            title = "T",
            author = "A",
            publisher = "P",
            publishedDate = null,
            coverUrl = null,
            totalPages = totalPages,
            price = null,
            category = null,
            tableOfContentsUrl = null,
            introductionUrl = null,
            summaryUrl = null,
        )

    private class RecordRepository : ReadingRecordRepository {
        var request: ReadingRecord? = null
        var updatedAt: Instant? = null
        var failure: Exception? = null
        var calls = 0
        var maxEndPage: Int? = null

        override suspend fun addRecord(
            record: ReadingRecord,
            updatedAt: Instant,
        ): Long {
            calls++
            failure?.let { throw it }
            request = record
            this.updatedAt = updatedAt
            return 7
        }

        override suspend fun saveRecord(record: ReadingRecord): Long = error("Not used")

        override suspend fun removeRecord(id: Long) = Unit

        override fun observeRecordsByIsbn(isbn: String): Flow<List<ReadingRecord>> = emptyFlow()

        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> = emptyFlow()

        override suspend fun getMaxEndPage(isbn: String): Int? = maxEndPage
    }

    private inner class BookRepository : com.leeseungyun1020.manicule.core.data.repository.BookRepository {
        val book = MutableStateFlow<Book?>(null)

        override fun observeBook(isbn: String): Flow<Book?> = book

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> = error("Not used")

        override fun searchBooks(query: String): Flow<PagingData<Book>> = emptyFlow()
    }

    private inner class LibraryRepository : com.leeseungyun1020.manicule.core.data.repository.LibraryRepository {
        var status: ReadingStatus? = null

        override fun observeAll(): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: LocalDate?,
        ): ReadingStatusChangeResult = ReadingStatusChangeResult.Changed

        override fun observeByStatus(
            status: ReadingStatus,
            sort: LibrarySort,
        ): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> = emptyList()

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> =
            MutableStateFlow(
                this.status?.let {
                    BookEntry(
                        book = bookRepo.book.value ?: return@let null,
                        status = it,
                        addedAt = Instant.fromEpochMilliseconds(0),
                        updatedAt = Instant.fromEpochMilliseconds(0),
                    )
                },
            )

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = SaveBookEntryResult.Saved

        override suspend fun removeBookEntry(isbn: String) = Unit

        override suspend fun restoreDeletedEntryIfAbsent(entry: BookEntry): Boolean = error("Not used")

        override suspend fun restoreReadingStatusIfUnchanged(
            original: BookEntry,
            changedStatus: ReadingStatus,
            changedAt: kotlinx.datetime.Instant,
        ): Boolean = error("Not used")
    }
}
