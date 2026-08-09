package com.leeseungyun1020.manicule.core.domain.stats

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Test

class GetReadingStreakUseCaseTest {
    private val today = LocalDate(2024, 3, 10)
    private val clock = FixedClock(Instant.parse("2024-03-10T12:00:00Z"), TimeZone.UTC)

    @Test
    fun current_streak_accepts_yesterday_and_longest_uses_all_past_dates() =
        runTest {
            val repository = FakeStatsRepository()
            repository.dates.value =
                listOf(
                    LocalDate(2024, 2, 1),
                    LocalDate(2024, 2, 2),
                    LocalDate(2024, 2, 3),
                    LocalDate(2024, 3, 8),
                    LocalDate(2024, 3, 9),
                )

            val streak = GetReadingStreakUseCase(repository, clock)().first()

            assertThat(streak.current).isEqualTo(2)
            assertThat(streak.longest).isEqualTo(3)
            assertThat(streak.lastDate).isEqualTo(LocalDate(2024, 3, 9))
            assertThat(repository.datesEnd).isEqualTo(today)
        }

    @Test
    fun stale_last_date_resets_current_and_future_dates_are_ignored() =
        runTest {
            val repository = FakeStatsRepository()
            repository.dates.value =
                listOf(
                    LocalDate(2024, 3, 1),
                    LocalDate(2024, 3, 2),
                    LocalDate(2024, 3, 12),
                )

            val streak = GetReadingStreakUseCase(repository, clock)().first()

            assertThat(streak.current).isEqualTo(0)
            assertThat(streak.longest).isEqualTo(2)
            assertThat(streak.lastDate).isEqualTo(LocalDate(2024, 3, 2))
        }

    @Test
    fun empty_dates_return_empty_streak() =
        runTest {
            assertThat(GetReadingStreakUseCase(FakeStatsRepository(), clock)().first())
                .isEqualTo(com.leeseungyun1020.manicule.core.model.ReadingStreak.Empty)
        }
}
