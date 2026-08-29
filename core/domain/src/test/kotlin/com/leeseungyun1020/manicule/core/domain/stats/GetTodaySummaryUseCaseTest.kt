package com.leeseungyun1020.manicule.core.domain.stats

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.model.ReadingTotals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
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

    @Test
    fun reobserves_totals_for_the_new_date_after_midnight() =
        runTest {
            val repository = FakeStatsRepository()
            val clock = MutableClock(Instant.parse("2024-03-01T23:59:59Z"), TimeZone.UTC)

            GetTodaySummaryUseCase(repository, clock)().test {
                assertThat(awaitItem().date).isEqualTo(LocalDate(2024, 3, 1))

                clock.instant = Instant.parse("2024-03-02T00:00:00Z")
                advanceTimeBy(1_000)
                runCurrent()

                assertThat(awaitItem().date).isEqualTo(LocalDate(2024, 3, 2))
                assertThat(repository.lastRange)
                    .isEqualTo(LocalDate(2024, 3, 2) to LocalDate(2024, 3, 2))
                cancelAndIgnoreRemainingEvents()
            }
        }
}
