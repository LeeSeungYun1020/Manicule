package com.leeseungyun1020.manicule.core.domain.library

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
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

class UpdateRatingUseCaseTest {
    private val repository = RatingRepository()
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
    private val useCase = UpdateRatingUseCase(repository, clock)

    @Test
    fun validRatings_callRepository_withCurrentTime() =
        runTest {
            (0..5).forEach { rating ->
                val result = useCase("123", rating)
                assertThat(result).isEqualTo(RatingChangeResult.Changed)
                assertThat(repository.request).isEqualTo(RatingRequest("123", rating, now))
            }
            assertThat(clockReads).isEqualTo(6)
        }

    @Test
    fun invalidRating_returnsInvalidRating_withoutAccessingClockOrRepository() =
        runTest {
            listOf(-1, 6, 10).forEach { rating ->
                val result = useCase("123", rating)
                assertThat(result).isEqualTo(RatingChangeResult.InvalidRating)
                assertThat(repository.request).isNull()
                assertThat(clockReads).isEqualTo(0)
            }
        }

    @Test
    fun repositoryResults_areForwarded() =
        runTest {
            listOf(RatingChangeResult.Unchanged, RatingChangeResult.BookNotFound).forEach {
                repository.result = it
                val result = useCase("123", 4)
                assertThat(result).isEqualTo(it)
            }
        }

    @Test
    fun cancellation_isNotCaught() =
        runTest {
            val cancellation = CancellationException("Cancelled")
            repository.failure = cancellation
            try {
                useCase("123", 4)
                error("Expected cancellation")
            } catch (actual: CancellationException) {
                assertThat(actual).isSameInstanceAs(cancellation)
            }
        }

    private data class RatingRequest(
        val isbn: String,
        val rating: Int,
        val updatedAt: Instant,
    )

    private class RatingRepository : LibraryRepository {
        var request: RatingRequest? = null
        var result = RatingChangeResult.Changed
        var failure: Exception? = null

        override suspend fun updateRating(
            isbn: String,
            rating: Int,
            updatedAt: Instant,
        ): RatingChangeResult {
            failure?.let { throw it }
            request = RatingRequest(isbn, rating, updatedAt)
            return result
        }

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
