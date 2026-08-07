package com.leeseungyun1020.manicule.core.model

import com.google.common.truth.Truth.assertThat
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertThrows
import org.junit.Test

class ReadingRecordTest {
    @Test
    fun `pagesRead includes both range endpoints`() {
        assertThat(record(startPage = 1, endPage = 10).pagesRead).isEqualTo(10)
        assertThat(record(startPage = 11, endPage = 42).pagesRead).isEqualTo(32)
        assertThat(record(startPage = 43, endPage = 68).pagesRead).isEqualTo(26)
    }

    @Test
    fun `pagesRead counts overlapping and reread sessions independently`() {
        val records =
            listOf(
                record(startPage = 1, endPage = 10),
                record(startPage = 8, endPage = 12),
                record(startPage = 1, endPage = 10),
            )

        assertThat(records.sumOf(ReadingRecord::pagesRead)).isEqualTo(25)
    }

    @Test
    fun `startPage must be at least one`() {
        assertThrows(IllegalArgumentException::class.java) {
            record(startPage = 0, endPage = 10)
        }
    }

    @Test
    fun `endPage must not precede startPage`() {
        assertThrows(IllegalArgumentException::class.java) {
            record(startPage = 10, endPage = 9)
        }
    }

    @Test
    fun `isbn must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            record(startPage = 1, endPage = 10, isbn = " ")
        }
    }

    private fun record(
        startPage: Int,
        endPage: Int,
        isbn: String = "123",
    ) = ReadingRecord(
        id = 0,
        isbn = isbn,
        date = LocalDate(2024, 4, 12),
        time = LocalTime(10, 30),
        startPage = startPage,
        endPage = endPage,
    )
}
