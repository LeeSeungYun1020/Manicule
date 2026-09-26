package com.leeseungyun1020.manicule.core.domain.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.domain.stats.FixedClock
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.DailyReading
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingTotals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Test

class ObserveHomeDataUseCaseTest {
    @Test
    fun summary_retry_resubscribes_only_summary_sources() =
        runTest {
            val library = CountingLibraryRepository()
            val stats = RetryingStatsRepository()
            val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
            val useCase = ObserveHomeDataUseCase(
                GetLibraryBooksUseCase(library),
                stats,
                FixedClock(Instant.parse("2026-09-22T00:00:00Z"), TimeZone.UTC),
            )

            useCase(retries).test {
                assertThat(awaitItem().summary).isNull()

                retries.tryEmit(Unit)

                assertThat(awaitItem().summary?.todayPages).isEqualTo(12)
                assertThat(stats.totalsSubscriptions).isEqualTo(2)
                assertThat(library.allSubscriptions).isEqualTo(1)
                assertThat(library.readingSubscriptions).isEqualTo(1)
                cancelAndIgnoreRemainingEvents()
            }
        }
}

private class CountingLibraryRepository : LibraryRepository {
    var allSubscriptions = 0
    var readingSubscriptions = 0

    override fun observeAll(): Flow<List<BookEntry>> =
        flow {
            allSubscriptions++
            emit(emptyList())
        }

    override fun observeByStatus(
        status: ReadingStatus,
        sort: LibrarySort,
    ): Flow<List<BookEntry>> =
        flow {
            readingSubscriptions++
            emit(emptyList())
        }

    override suspend fun changeReadingStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Instant,
        finishedAt: LocalDate?,
    ): ReadingStatusChangeResult = error("Not used")

    override suspend fun updateRating(
        isbn: String,
        rating: Int,
        updatedAt: Instant,
    ): RatingChangeResult = error("Not used")

    override suspend fun updateMemo(
        isbn: String,
        memo: String?,
        updatedAt: Instant,
    ): com.leeseungyun1020.manicule.core.model.MemoChangeResult = error("Not used")

    override suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book> = error("Not used")

    override fun observeBookEntry(isbn: String): Flow<BookEntry?> = error("Not used")

    override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult = error("Not used")

    override suspend fun removeBookEntry(isbn: String) = error("Not used")

    override suspend fun restoreDeletedEntryIfAbsent(entry: BookEntry): Boolean = error("Not used")

    override suspend fun restoreReadingStatusIfUnchanged(
        original: BookEntry,
        changedStatus: ReadingStatus,
        changedAt: kotlinx.datetime.Instant,
    ): Boolean = error("Not used")
}

private class RetryingStatsRepository : StatsRepository {
    var totalsSubscriptions = 0

    override fun observeTotals(
        start: LocalDate,
        end: LocalDate,
    ): Flow<ReadingTotals> =
        flow {
            totalsSubscriptions++
            if (totalsSubscriptions == 1) error("Summary unavailable")
            emit(ReadingTotals(pagesRead = 12, bookCount = 1))
        }

    override fun observeDailyReading(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DailyReading>> = flowOf(emptyList())

    override fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>> = flowOf(listOf(end))

    override fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecord>> = error("Not used")
}
