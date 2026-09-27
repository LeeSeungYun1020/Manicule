package com.leeseungyun1020.manicule.core.domain.record

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Test

class EditReadingRecordUseCaseTest {
    private val repository = FakeReadingRecordRepository()
    private val now = Instant.parse("2026-09-16T12:00:00Z")
    private val clock =
        object : Clock {
            override fun now(): Instant = now

            override fun timeZone(): TimeZone = TimeZone.UTC
        }
    private val useCase = EditReadingRecordUseCase(repository, clock)

    @Test
    fun invoke_with_valid_positive_id_returns_repository_result() =
        runTest {
            val record =
                ReadingRecord(
                    id = 1L,
                    isbn = "123",
                    date = LocalDate(2024, 4, 12),
                    time = LocalTime(10, 0),
                    startPage = 1,
                    endPage = 20,
                )
            repository.saveResult = true

            val result = useCase(record)

            assertThat(result).isTrue()
            assertThat(repository.savedRecord).isEqualTo(record)
            assertThat(repository.savedUpdatedAt).isEqualTo(now)
        }

    @Test
    fun invoke_with_non_positive_id_returns_false_without_calling_repository() =
        runTest {
            val record =
                ReadingRecord(
                    id = 0L,
                    isbn = "123",
                    date = LocalDate(2024, 4, 12),
                    time = LocalTime(10, 0),
                    startPage = 1,
                    endPage = 20,
                )

            val result = useCase(record)

            assertThat(result).isFalse()
            assertThat(repository.savedRecord).isNull()
        }

    @Test
    fun invoke_when_repository_returns_false_returns_false() =
        runTest {
            val record =
                ReadingRecord(
                    id = 999L,
                    isbn = "123",
                    date = LocalDate(2024, 4, 12),
                    time = LocalTime(10, 0),
                    startPage = 1,
                    endPage = 20,
                )
            repository.saveResult = false

            val result = useCase(record)

            assertThat(result).isFalse()
            assertThat(repository.savedRecord).isEqualTo(record)
            assertThat(repository.savedUpdatedAt).isEqualTo(now)
        }

    private class FakeReadingRecordRepository : ReadingRecordRepository {
        var savedRecord: ReadingRecord? = null
        var savedUpdatedAt: Instant? = null
        var saveResult = true

        override suspend fun addRecord(
            record: ReadingRecord,
            updatedAt: Instant,
        ): Long = error("Not used")

        override suspend fun saveRecord(
            record: ReadingRecord,
            updatedAt: Instant,
        ): Boolean {
            savedRecord = record
            savedUpdatedAt = updatedAt
            return saveResult
        }

        override suspend fun removeRecord(
            id: Long,
            isbn: String,
            updatedAt: Instant,
        ): Boolean = error("Not used")

        override fun observeRecordsByIsbn(isbn: String): Flow<List<ReadingRecord>> = emptyFlow()

        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> = emptyFlow()

        override suspend fun getMaxEndPage(isbn: String): Int? = null
    }
}
