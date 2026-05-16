package com.leeseungyun1020.manicule.core.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ContributionDayTest {

    @Test
    fun `intensity 0 when pages is zero`() {
        assertThat(ContributionDay.intensityOf(0)).isEqualTo(0)
    }

    @Test
    fun `intensity 1 when pages is in 1 to 19`() {
        assertThat(ContributionDay.intensityOf(1)).isEqualTo(1)
        assertThat(ContributionDay.intensityOf(19)).isEqualTo(1)
    }

    @Test
    fun `intensity 2 when pages is in 20 to 49`() {
        assertThat(ContributionDay.intensityOf(20)).isEqualTo(2)
        assertThat(ContributionDay.intensityOf(49)).isEqualTo(2)
    }

    @Test
    fun `intensity 3 when pages is in 50 to 99`() {
        assertThat(ContributionDay.intensityOf(50)).isEqualTo(3)
        assertThat(ContributionDay.intensityOf(99)).isEqualTo(3)
    }

    @Test
    fun `intensity 4 when pages is 100 or more`() {
        assertThat(ContributionDay.intensityOf(100)).isEqualTo(4)
        assertThat(ContributionDay.intensityOf(1000)).isEqualTo(4)
    }
}
