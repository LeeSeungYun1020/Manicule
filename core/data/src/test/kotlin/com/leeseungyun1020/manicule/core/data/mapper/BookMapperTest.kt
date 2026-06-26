package com.leeseungyun1020.manicule.core.data.mapper

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkBookDto
import kotlinx.datetime.LocalDate
import org.junit.Test

class BookMapperTest {

    @Test
    fun bookEntity_asExternalModel_mapsCorrectly() {
        // given
        val entity =
            BookEntity(
                isbn = "9791170613114",
                title = "시대예보",
                author = "저자: 송길영",
                publisher = "교보문고",
                publishedDate = LocalDate(2025, 9, 11),
                coverUrl = null,
                totalPages = 358,
                price = 22000,
                category = "3",
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            )

        // when
        val book = entity.asExternalModel()

        // then
        assertThat(book.isbn).isEqualTo(entity.isbn)
        assertThat(book.title).isEqualTo(entity.title)
        assertThat(book.author).isEqualTo(entity.author)
        assertThat(book.publisher).isEqualTo(entity.publisher)
        assertThat(book.publishedDate).isEqualTo(entity.publishedDate)
        assertThat(book.totalPages).isEqualTo(entity.totalPages)
        assertThat(book.price).isEqualTo(entity.price)
        assertThat(book.category).isEqualTo(entity.category)
        assertThat(book.coverUrl).isNull()
    }

    @Test
    fun nlkBookDto_asExternalModel_mapsCorrectly() {
        // given
        val dto =
            NlkBookDto(
                isbn = "9791170613114",
                title = "시대예보",
                author = "저자: 송길영",
                publisher = "교보문고",
                publishPredate = "20250911",
                titleUrl = "",
                page = "358 p.",
                prePrice = "22000",
                subject = "3",
                bookTbCntUrl = "",
                bookIntroductionUrl = "",
                bookSummaryUrl = "",
            )

        // when
        val book = dto.asExternalModel()

        // then
        assertThat(book.isbn).isEqualTo("9791170613114")
        assertThat(book.title).isEqualTo("시대예보")
        assertThat(book.author).isEqualTo("저자: 송길영")
        assertThat(book.publisher).isEqualTo("교보문고")
        assertThat(book.publishedDate).isEqualTo(LocalDate(2025, 9, 11))
        assertThat(book.totalPages).isEqualTo(358)
        assertThat(book.price).isEqualTo(22000)
        assertThat(book.category).isEqualTo("3")
        assertThat(book.coverUrl).isNull()
    }

    @Test
    fun nlkBookDto_withEmptyUrl_mapsToNull() {
        // given
        val dto =
            NlkBookDto(
                isbn = "123",
                titleUrl = "",
                bookTbCntUrl = "  ",
                bookIntroductionUrl = "",
                bookSummaryUrl = "",
            )

        // when
        val book = dto.asExternalModel()

        // then
        assertThat(book.coverUrl).isNull()
        assertThat(book.tableOfContentsUrl).isNull()
        assertThat(book.introductionUrl).isNull()
        assertThat(book.summaryUrl).isNull()
    }

    @Test
    fun parseNlkDate_validFormat_returnsLocalDate() {
        val date = parseNlkDate("20240412")
        assertThat(date).isEqualTo(LocalDate(2024, 4, 12))
    }

    @Test
    fun parseNlkDate_invalidFormat_returnsNull() {
        assertThat(parseNlkDate("")).isNull()
        assertThat(parseNlkDate("2024")).isNull()
        assertThat(parseNlkDate("invalid!")).isNull()
        assertThat(parseNlkDate("20241301")).isNull() // Invalid month
    }
}
