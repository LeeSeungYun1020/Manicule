package com.leeseungyun1020.manicule.core.domain.stats

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.model.DailyReading
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.Test

class GetReadingCalendarUseCaseTest {
    @Test
    fun fills_missing_leap_day_range_and_applies_five_intensities() =
        runTest {
            val repository = FakeStatsRepository()
            repository.daily.value =
                listOf(
                    DailyReading(LocalDate(2024, 2, 28), 19, 1),
                    DailyReading(LocalDate(2024, 3, 1), 100, 2),
                )
            val useCase = GetReadingCalendarUseCase(repository)

            val days = useCase(LocalDate(2024, 2, 28), LocalDate(2024, 3, 1)).first()

            assertThat(days.map { it.date })
                .containsExactly(LocalDate(2024, 2, 28), LocalDate(2024, 2, 29), LocalDate(2024, 3, 1))
                .inOrder()
            assertThat(days.map { it.pages }).containsExactly(19, 0, 100).inOrder()
            assertThat(days.map { it.intensity }).containsExactly(1, 0, 4).inOrder()
        }

    @Test
    fun reversed_range_is_rejected() {
        val useCase = GetReadingCalendarUseCase(FakeStatsRepository())

        assertThat(
            runCatching { useCase(LocalDate(2024, 3, 1), LocalDate(2024, 2, 29)) }.exceptionOrNull(),
        ).isInstanceOf(IllegalArgumentException::class.java)
    }
}
