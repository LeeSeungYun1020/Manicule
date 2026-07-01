package com.leeseungyun1020.manicule.core.data.mapper

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.junit.Test

class BookEntryMapperTest {

    @Test
    fun bookEntryWithCurrentPage_asExternalModel_mapsCorrectly() {
        // given
        val bookEntity =
            BookEntity(
                isbn = "12345",
                title = "Test Book",
                author = "Test Author",
                publisher = "Test Publisher",
                publishedDate = LocalDate(2024, 1, 1),
                coverUrl = null,
                totalPages = 200,
                price = 15000,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            )

        val entryEntity =
            BookEntryEntity(
                isbn = "12345",
                status = ReadingStatus.READING,
                rating = 4,
                memo = "Good book",
                addedAt = Instant.fromEpochMilliseconds(1000),
                updatedAt = Instant.fromEpochMilliseconds(2000),
                finishedAt = null,
            )

        val projection =
            BookEntryWithCurrentPage(
                entry = entryEntity,
                book = bookEntity,
                currentPage = 50,
            )

        // when
        val bookEntry = projection.asExternalModel()

        // then
        assertThat(bookEntry.book.isbn).isEqualTo("12345")
        assertThat(bookEntry.status).isEqualTo(ReadingStatus.READING)
        assertThat(bookEntry.rating).isEqualTo(4)
        assertThat(bookEntry.memo).isEqualTo("Good book")
        assertThat(bookEntry.currentPage).isEqualTo(50)
    }
}
