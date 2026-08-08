package com.leeseungyun1020.manicule.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ReadingCalendarDayTest {

    @Test
    fun `intensity 0 when pages is zero`() {
        assertThat(ReadingCalendarDay.intensityOf(0)).isEqualTo(0)
    }

    @Test
    fun `intensity 1 when pages is in 1 to 19`() {
        assertThat(ReadingCalendarDay.intensityOf(1)).isEqualTo(1)
        assertThat(ReadingCalendarDay.intensityOf(19)).isEqualTo(1)
    }

    @Test
    fun `intensity 2 when pages is in 20 to 49`() {
        assertThat(ReadingCalendarDay.intensityOf(20)).isEqualTo(2)
        assertThat(ReadingCalendarDay.intensityOf(49)).isEqualTo(2)
    }

    @Test
    fun `intensity 3 when pages is in 50 to 99`() {
        assertThat(ReadingCalendarDay.intensityOf(50)).isEqualTo(3)
        assertThat(ReadingCalendarDay.intensityOf(99)).isEqualTo(3)
    }

    @Test
    fun `intensity 4 when pages is 100 or more`() {
        assertThat(ReadingCalendarDay.intensityOf(100)).isEqualTo(4)
        assertThat(ReadingCalendarDay.intensityOf(1000)).isEqualTo(4)
    }
}
