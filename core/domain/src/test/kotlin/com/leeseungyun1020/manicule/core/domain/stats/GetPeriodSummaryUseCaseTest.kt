package com.leeseungyun1020.manicule.core.domain.stats

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Test

class GetPeriodSummaryUseCaseTest {
    @Test
    fun summarizes_inclusive_range_with_period_longest_streak() =
        runTest {
            val repository = FakeStatsRepository()
            repository.records.value =
                listOf(
                    record(1, "a", LocalDate(2024, 2, 28), 1, 10),
                    record(2, "a", LocalDate(2024, 2, 29), 11, 30),
                    record(3, "a", LocalDate(2024, 3, 2), 31, 40),
                    record(4, "b", LocalDate(2024, 3, 2), 1, 20),
                )
            val start = LocalDate(2024, 2, 28)
            val end = LocalDate(2024, 3, 2)

            val summary = GetPeriodSummaryUseCase(repository)(start, end).first()

            assertThat(summary.rangeStart).isEqualTo(start)
            assertThat(summary.rangeEnd).isEqualTo(end)
            assertThat(summary.longestStreak).isEqualTo(2)
            assertThat(summary.pagesRead).isEqualTo(60)
            assertThat(summary.bookCount).isEqualTo(2)
        }

    @Test
    fun empty_range_has_zero_summary() =
        runTest {
            val date = LocalDate(2024, 1, 1)

            val summary = GetPeriodSummaryUseCase(FakeStatsRepository())(date, date).first()

            assertThat(summary.longestStreak).isEqualTo(0)
            assertThat(summary.pagesRead).isEqualTo(0)
            assertThat(summary.bookCount).isEqualTo(0)
        }

    @Test
    fun reversed_range_is_rejected() {
        val useCase = GetPeriodSummaryUseCase(FakeStatsRepository())

        assertThat(
            runCatching { useCase(LocalDate(2024, 1, 2), LocalDate(2024, 1, 1)) }.exceptionOrNull(),
        ).isInstanceOf(IllegalArgumentException::class.java)
    }

    private fun record(
        id: Long,
        isbn: String,
        date: LocalDate,
        startPage: Int,
        endPage: Int,
    ) = ReadingRecord(
        id = id,
        isbn = isbn,
        date = date,
        time = LocalTime(12, 0),
        startPage = startPage,
        endPage = endPage,
    )
}
