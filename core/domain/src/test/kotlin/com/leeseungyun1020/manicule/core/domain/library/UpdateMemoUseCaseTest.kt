package com.leeseungyun1020.manicule.core.domain.library

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.MemoChangeResult
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
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

class UpdateMemoUseCaseTest {
    private val repository = MemoRepository()
    private val now = Instant.parse("2026-09-04T16:00:00Z")
    private var clockReads = 0
    private val clock =
        object : Clock {
            override fun now(): Instant {
                clockReads++
                return now
            }

            override fun timeZone(): TimeZone = TimeZone.of("Asia/Seoul")
        }
    private val useCase = UpdateMemoUseCase(repository, clock)

    @Test
    fun validMemo_callsRepository_withCurrentTime_andTrimmedMemo() =
        runTest {
            val result = useCase("123", "  좋은 책이었습니다.  \n")
            assertThat(result).isEqualTo(MemoChangeResult.Changed)
            assertThat(repository.request).isEqualTo(MemoRequest("123", "좋은 책이었습니다.", now))
            assertThat(clockReads).isEqualTo(1)
        }

    @Test
    fun blankOrEmptyMemo_normalizesToNull() =
        runTest {
            listOf("", "   ", "\t\n  ").forEach { blankMemo ->
                val result = useCase("123", blankMemo)
                assertThat(result).isEqualTo(MemoChangeResult.Changed)
                assertThat(repository.request).isEqualTo(MemoRequest("123", null, now))
            }
        }

    @Test
    fun nullMemo_callsRepository_withNull() =
        runTest {
            val result = useCase("123", null)
            assertThat(result).isEqualTo(MemoChangeResult.Changed)
            assertThat(repository.request).isEqualTo(MemoRequest("123", null, now))
        }

    @Test
    fun repositoryResults_areForwarded() =
        runTest {
            listOf(MemoChangeResult.Unchanged, MemoChangeResult.BookNotFound).forEach {
                repository.result = it
                val result = useCase("123", "메모")
                assertThat(result).isEqualTo(it)
            }
        }

    @Test
    fun cancellation_isNotCaught() =
        runTest {
            val cancellation = CancellationException("Cancelled")
            repository.failure = cancellation
            try {
                useCase("123", "메모")
                error("Expected cancellation")
            } catch (actual: CancellationException) {
                assertThat(actual).isSameInstanceAs(cancellation)
            }
        }

    private data class MemoRequest(
        val isbn: String,
        val memo: String?,
        val updatedAt: Instant,
    )

    private class MemoRepository : LibraryRepository {
        var request: MemoRequest? = null
        var result = MemoChangeResult.Changed
        var failure: Exception? = null

        override suspend fun updateMemo(
            isbn: String,
            memo: String?,
            updatedAt: Instant,
        ): MemoChangeResult {
            failure?.let { throw it }
            request = MemoRequest(isbn, memo, updatedAt)
            return result
        }

        override suspend fun updateRating(
            isbn: String,
            rating: Int,
            updatedAt: Instant,
        ): RatingChangeResult = error("Not used")

        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: LocalDate?,
        ): ReadingStatusChangeResult = error("Not used")

        override fun observeAll(): Flow<List<BookEntry>> = emptyFlow()

        override fun observeByStatus(
            status: ReadingStatus,
            sort: LibrarySort,
        ): Flow<List<BookEntry>> = emptyFlow()

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> = emptyList()

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> = emptyFlow()

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = error("Not used")

        override suspend fun restoreDeletedEntryIfAbsent(entry: BookEntry): Boolean = error("Not used")

        override suspend fun restoreReadingStatusIfUnchanged(
            original: BookEntry,
            changedStatus: ReadingStatus,
            changedAt: Instant,
        ): Boolean = error("Not used")

        override suspend fun removeBookEntry(isbn: String) = Unit
    }
}
