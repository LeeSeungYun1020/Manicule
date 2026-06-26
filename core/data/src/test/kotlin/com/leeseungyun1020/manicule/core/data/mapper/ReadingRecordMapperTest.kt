package com.leeseungyun1020.manicule.core.data.mapper

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.datetime.LocalDate
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
                cumulativePage = 100,
            )

        // when
        val record = entity.asExternalModel()

        // then
        assertThat(record.id).isEqualTo(1L)
        assertThat(record.isbn).isEqualTo("12345")
        assertThat(record.date).isEqualTo(LocalDate(2024, 4, 12))
        assertThat(record.cumulativePage).isEqualTo(100)
    }
}
