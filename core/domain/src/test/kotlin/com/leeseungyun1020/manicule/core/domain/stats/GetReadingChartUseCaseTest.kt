package com.leeseungyun1020.manicule.core.domain.stats

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GetReadingChartUseCaseTest {
    private val repository = FakeStatsRepository()

    @Test fun weeklyBucketsClipEndsAndDeduplicateEachWeek() =
        runTest {
            val start = LocalDate(2024, 2, 29)
            val end = LocalDate(2024, 3, 12)
            repository.records.value = listOf(
                record(1, LocalDate(2024, 2, 29), "a", 10),
                record(2, LocalDate(2024, 3, 1), "a", 20),
                record(3, LocalDate(2024, 3, 4), "a", 30),
                record(4, LocalDate(2024, 3, 4), "b", 40),
                record(5, LocalDate(2024, 3, 13), "outside", 50),
            )
            val useCase = GetReadingChartUseCase(repository, StandardTestDispatcher(testScheduler))
            val buckets = useCase(start, end, ReadingChartUnit.WEEK).first()

            assertThat(buckets).containsExactly(
                ReadingChartBucket(start, LocalDate(2024, 3, 3), 1, 30),
                ReadingChartBucket(LocalDate(2024, 3, 4), LocalDate(2024, 3, 10), 2, 70),
                ReadingChartBucket(LocalDate(2024, 3, 11), end, 0, 0),
            ).inOrder()
            assertThat(repository.lastRange).isEqualTo(start to end)
        }

    @Test fun monthAndDayBucketsIncludeEmptyIntervals() {
        val records = listOf(record(1, LocalDate(2024, 2, 29), "a", 12))
        assertThat(aggregate(LocalDate(2024, 1, 31), LocalDate(2024, 3, 1), ReadingChartUnit.MONTH, records))
            .containsExactly(
                ReadingChartBucket(LocalDate(2024, 1, 31), LocalDate(2024, 1, 31), 0, 0),
                ReadingChartBucket(LocalDate(2024, 2, 1), LocalDate(2024, 2, 29), 1, 12),
                ReadingChartBucket(LocalDate(2024, 3, 1), LocalDate(2024, 3, 1), 0, 0),
            ).inOrder()
        assertThat(aggregate(LocalDate(2024, 2, 28), LocalDate(2024, 3, 1), ReadingChartUnit.DAY, records))
            .hasSize(3)
    }

    @Test fun updatesAreReaggregatedAndPageTotalsUseLong() =
        runTest {
            val date = LocalDate(2024, 3, 1)
            val useCase = GetReadingChartUseCase(repository, StandardTestDispatcher(testScheduler))
            val emissions = mutableListOf<List<ReadingChartBucket>>()
            val job = backgroundScope.launch { useCase(date, date, ReadingChartUnit.DAY).take(3).toList(emissions) }
            runCurrent()
            repository.records.value = listOf(record(1, date, "a", Int.MAX_VALUE), record(2, date, "a", 10))
            runCurrent()
            repository.records.value = emptyList()
            runCurrent()

            assertThat(emissions.map { it.single() }).containsExactly(
                ReadingChartBucket(date, date, 0, 0),
                ReadingChartBucket(date, date, 1, Int.MAX_VALUE.toLong() + 10),
                ReadingChartBucket(date, date, 0, 0),
            ).inOrder()
            job.cancel()
        }

    @Test fun invalidRangeFails() =
        runTest {
            val useCase = GetReadingChartUseCase(repository, StandardTestDispatcher(testScheduler))
            org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
                useCase(LocalDate(2024, 3, 2), LocalDate(2024, 3, 1), ReadingChartUnit.DAY)
            }
        }

    private fun record(
        id: Long,
        date: LocalDate,
        isbn: String,
        pages: Int,
    ) = ReadingRecord(id, isbn, date, LocalTime(12, 0), 1, pages)
}
