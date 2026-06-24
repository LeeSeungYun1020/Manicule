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
                currentPage = 50,
            )

        // when
        val bookEntry = projection.asExternalModel(bookEntity)

        // then
        assertThat(bookEntry.book.isbn).isEqualTo("12345")
        assertThat(bookEntry.status).isEqualTo(ReadingStatus.READING)
        assertThat(bookEntry.rating).isEqualTo(4)
        assertThat(bookEntry.memo).isEqualTo("Good book")
        assertThat(bookEntry.currentPage).isEqualTo(50)
    }

    @Test(expected = IllegalArgumentException::class)
    fun bookEntryWithCurrentPage_asExternalModel_throwsWhenIsbnMismatch() {
        // given
        val bookEntity =
            BookEntity(
                isbn = "99999",
                title = "Test Book",
                author = "Test Author",
                publisher = "Test Publisher",
                publishedDate = null,
                coverUrl = null,
                totalPages = null,
                price = null,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            )

        val entryEntity =
            BookEntryEntity(
                isbn = "12345",
                status = ReadingStatus.WANT,
                rating = null,
                memo = null,
                addedAt = Instant.fromEpochMilliseconds(1000),
                updatedAt = Instant.fromEpochMilliseconds(2000),
                finishedAt = null,
            )

        val projection =
            BookEntryWithCurrentPage(
                entry = entryEntity,
                currentPage = null,
            )

        // when
        projection.asExternalModel(bookEntity)
    }
}
