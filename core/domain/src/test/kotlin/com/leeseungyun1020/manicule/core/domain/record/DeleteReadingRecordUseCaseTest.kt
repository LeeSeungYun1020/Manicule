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
import kotlinx.datetime.TimeZone
import org.junit.Test

class DeleteReadingRecordUseCaseTest {
    private val repository = FakeReadingRecordRepository()
    private val now = Instant.parse("2026-09-16T12:00:00Z")
    private val clock =
        object : Clock {
            override fun now(): Instant = now

            override fun timeZone(): TimeZone = TimeZone.UTC
        }
    private val useCase = DeleteReadingRecordUseCase(repository, clock)

    @Test
    fun invoke_with_valid_id_and_isbn_returns_repository_result() =
        runTest {
            repository.removeResult = true

            val result = useCase(1L, "123")

            assertThat(result).isTrue()
            assertThat(repository.removedArgs).isEqualTo(Triple(1L, "123", now))
        }

    @Test
    fun invoke_with_non_positive_id_returns_false_without_calling_repository() =
        runTest {
            val zeroResult = useCase(0L, "123")
            val negativeResult = useCase(-1L, "123")

            assertThat(zeroResult).isFalse()
            assertThat(negativeResult).isFalse()
            assertThat(repository.removedArgs).isNull()
        }

    @Test
    fun invoke_with_blank_isbn_returns_false_without_calling_repository() =
        runTest {
            val blankResult = useCase(1L, "   ")

            assertThat(blankResult).isFalse()
            assertThat(repository.removedArgs).isNull()
        }

    @Test
    fun invoke_when_repository_returns_false_returns_false() =
        runTest {
            repository.removeResult = false

            val result = useCase(999L, "123")

            assertThat(result).isFalse()
            assertThat(repository.removedArgs).isEqualTo(Triple(999L, "123", now))
        }

    private class FakeReadingRecordRepository : ReadingRecordRepository {
        var removedArgs: Triple<Long, String, Instant>? = null
        var removeResult = true

        override suspend fun addRecord(
            record: ReadingRecord,
            updatedAt: Instant,
        ): Long = error("Not used")

        override suspend fun saveRecord(
            record: ReadingRecord,
            updatedAt: Instant,
        ): Boolean = error("Not used")

        override suspend fun removeRecord(
            id: Long,
            isbn: String,
            updatedAt: Instant,
        ): Boolean {
            removedArgs = Triple(id, isbn, updatedAt)
            return removeResult
        }

        override fun observeRecordsByIsbn(isbn: String): Flow<List<ReadingRecord>> = emptyFlow()

        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> = emptyFlow()

        override suspend fun getMaxEndPage(isbn: String): Int? = null
    }
}
