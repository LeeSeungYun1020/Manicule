package com.leeseungyun1020.manicule.core.common.time

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import org.junit.Test

class DateExtTest {

    @Test
    fun `startOfWeek returns previous sunday for a wednesday`() {
        // 2026-04-29 is Wednesday
        val date = LocalDate(2026, 4, 29)
        assertThat(date.startOfWeek()).isEqualTo(LocalDate(2026, 4, 26))
    }

    @Test
    fun `startOfWeek returns same day for sunday`() {
        // 2026-04-26 is Sunday
        val date = LocalDate(2026, 4, 26)
        assertThat(date.startOfWeek()).isEqualTo(date)
    }

    @Test
    fun `endOfMonth returns last day of february leap year`() {
        val date = LocalDate(2024, 2, 10)
        assertThat(date.endOfMonth()).isEqualTo(LocalDate(2024, 2, 29))
    }

    @Test
    fun `dateRangeInclusive iterates all days inclusive`() {
        val start = LocalDate(2026, 5, 1)
        val end = LocalDate(2026, 5, 3)
        assertThat(dateRangeInclusive(start, end).toList())
            .containsExactly(
                LocalDate(2026, 5, 1),
                LocalDate(2026, 5, 2),
                LocalDate(2026, 5, 3),
            ).inOrder()
    }

    @Test(expected = IllegalArgumentException::class)
    fun `dateRangeInclusive throws when start is after endInclusive`() {
        dateRangeInclusive(LocalDate(2026, 5, 10), LocalDate(2026, 5, 1))
    }
}
