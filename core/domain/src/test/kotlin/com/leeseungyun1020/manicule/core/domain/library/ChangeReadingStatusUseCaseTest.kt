package com.leeseungyun1020.manicule.core.domain.library

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Test

class ChangeReadingStatusUseCaseTest {
    private val repository = StatusRepository()
    private val now = Instant.parse("2026-09-04T16:00:00Z")
    private var clockReads = 0
    private val clock = object : Clock {
        override fun now(): Instant {
            clockReads++
            return now
        }

        override fun timeZone(): TimeZone = TimeZone.of("Asia/Seoul")
    }
    private val useCase = ChangeReadingStatusUseCase(repository, clock)

    @Test
    fun finished_usesLocalDateFromOneInstant() =
        runTest {
            assertThat(useCase("123", ReadingStatus.FINISHED)).isEqualTo(ReadingStatusChangeResult.Changed)
            assertThat(repository.request).isEqualTo(Request("123", ReadingStatus.FINISHED, now, LocalDate(2026, 9, 5)))
            assertThat(clockReads).isEqualTo(1)
        }

    @Test
    fun unfinishedStatuses_clearFinishedDate() =
        runTest {
            listOf(ReadingStatus.WANT, ReadingStatus.READING).forEach { status ->
                useCase("123", status)
                assertThat(repository.request).isEqualTo(Request("123", status, now, null))
            }
        }

    @Test
    fun unset_isRejectedWithoutStorageOrClockAccess() =
        runTest {
            assertThat(useCase("123", ReadingStatus.UNSET)).isEqualTo(ReadingStatusChangeResult.InvalidStatus)
            assertThat(repository.request).isNull()
            assertThat(clockReads).isEqualTo(0)
        }

    @Test
    fun storageResults_arePreserved() =
        runTest {
            listOf(ReadingStatusChangeResult.Unchanged, ReadingStatusChangeResult.BookNotFound).forEach {
                repository.result = it
                assertThat(useCase("123", ReadingStatus.READING)).isEqualTo(it)
            }
        }

    @Test
    fun cancellation_isNotConvertedToFailure() =
        runTest {
            val cancellation = CancellationException("Cancelled")
            repository.failure = cancellation
            try {
                useCase("123", ReadingStatus.READING)
                error("Expected cancellation")
            } catch (actual: CancellationException) {
                assertThat(actual).isSameInstanceAs(cancellation)
            }
        }

    private data class Request(
        val isbn: String,
        val status: ReadingStatus,
        val updatedAt: Instant,
        val finishedAt: LocalDate?,
    )

    private class StatusRepository : LibraryRepository {
        var request: Request? = null
        var result = ReadingStatusChangeResult.Changed
        var failure: Exception? = null

        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: LocalDate?,
        ): ReadingStatusChangeResult {
            failure?.let { throw it }
            request = Request(isbn, status, updatedAt, finishedAt)
            return result
        }

        override fun observeAll(): Flow<List<BookEntry>> = emptyFlow()

        override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> = emptyList()

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> = emptyFlow()

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = error("Not used")

        override suspend fun removeBookEntry(isbn: String) = Unit
    }
}
