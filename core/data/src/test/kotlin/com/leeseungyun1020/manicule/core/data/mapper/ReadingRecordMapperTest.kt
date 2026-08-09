package com.leeseungyun1020.manicule.core.data.mapper

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Test

class ReadingRecordMapperTest {

    @Test
    fun readingRecordEntity_asExternalModel_mapsCorrectly() {
        // given
        val entity =
            ReadingRecordEntity(
                id = 1L,
                isbn = "12345",
                date = LocalDate(2024, 4, 12),
                time = LocalTime(9, 30),
                startPage = 51,
                endPage = 100,
            )

        // when
        val record = entity.asExternalModel()

        // then
        assertThat(record.id).isEqualTo(1L)
        assertThat(record.isbn).isEqualTo("12345")
        assertThat(record.date).isEqualTo(LocalDate(2024, 4, 12))
        assertThat(record.time).isEqualTo(LocalTime(9, 30))
        assertThat(record.startPage).isEqualTo(51)
        assertThat(record.endPage).isEqualTo(100)
        assertThat(record.pagesRead).isEqualTo(50)
    }

    @Test
    fun readingRecord_asEntity_mapsCorrectly() {
        val record =
            ReadingRecord(
                id = 2L,
                isbn = "54321",
                date = LocalDate(2025, 1, 1),
                time = LocalTime(21, 15),
                startPage = 151,
                endPage = 200,
            )
        val entity = record.asEntity()
        assertThat(entity.id).isEqualTo(2L)
        assertThat(entity.isbn).isEqualTo("54321")
        assertThat(entity.date).isEqualTo(LocalDate(2025, 1, 1))
        assertThat(entity.time).isEqualTo(LocalTime(21, 15))
        assertThat(entity.startPage).isEqualTo(151)
        assertThat(entity.endPage).isEqualTo(200)
    }
}
