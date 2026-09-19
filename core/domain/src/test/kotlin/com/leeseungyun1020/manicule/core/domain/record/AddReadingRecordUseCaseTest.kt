package com.leeseungyun1020.manicule.core.domain.record

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Test

class AddReadingRecordUseCaseTest {
    private val repository = RecordRepository()
    private val now = Instant.parse("2026-09-16T12:00:00Z")
    private var clockReads = 0
    private val clock = object : Clock {
        override fun now(): Instant {
            clockReads++
            return now
        }

        override fun timeZone(): TimeZone = TimeZone.UTC
    }
    private val useCase = AddReadingRecordUseCase(repository, clock)
    private val date = LocalDate(2026, 9, 15)
    private val time = LocalTime(21, 30)

    @Test
    fun addsNewSessionWithSelectedDateTimeAndCurrentModificationTime() =
        runTest {
            assertThat(useCase("123", date, time, 11, 42)).isEqualTo(7L)
            assertThat(repository.request).isEqualTo(ReadingRecord(0, "123", date, time, 11, 42))
            assertThat(repository.request?.pagesRead).isEqualTo(32)
            assertThat(repository.updatedAt).isEqualTo(now)
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
                    assertThat(repository.request).isNull()
                    assertThat(clockReads).isEqualTo(0)
                }
            }
        }

    @Test
    fun storageFailureAndCancellationPropagateWithoutRetry() =
        runTest {
            listOf(IllegalStateException("Storage failure"), CancellationException("Cancelled")).forEach { failure ->
                repository.failure = failure
                try {
                    useCase("123", date, time, 1, 10)
                    error("Expected failure")
                } catch (actual: Exception) {
                    assertThat(actual).isSameInstanceAs(failure)
                }
            }
            assertThat(repository.calls).isEqualTo(2)
        }

    private class RecordRepository : ReadingRecordRepository {
        var request: ReadingRecord? = null
        var updatedAt: Instant? = null
        var failure: Exception? = null
        var calls = 0

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

        override suspend fun getMaxEndPage(isbn: String): Int? = null
    }
}
