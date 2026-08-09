package com.leeseungyun1020.manicule.core.domain.stats

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.model.ReadingTotals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Test

class GetTodaySummaryUseCaseTest {
    @Test
    fun uses_clock_timezone_and_returns_empty_totals() =
        runTest {
            val repository = FakeStatsRepository()
            repository.totals.value = ReadingTotals(0, 0)
            val useCase =
                GetTodaySummaryUseCase(
                    repository,
                    FixedClock(Instant.parse("2024-03-01T23:30:00Z"), TimeZone.of("Asia/Seoul")),
                )

            val summary = useCase().first()

            assertThat(summary.date).isEqualTo(LocalDate(2024, 3, 2))
            assertThat(summary.pagesRead).isEqualTo(0)
            assertThat(summary.bookCount).isEqualTo(0)
            assertThat(repository.lastRange).isEqualTo(summary.date to summary.date)
        }
}
