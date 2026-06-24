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
                isbn = "9788966263158",
                title = "소프트웨어 아키텍처 101",
                author = "마크 리처즈, 닐 포드",
                publisher = "한빛미디어",
                publishedDate = LocalDate(2021, 11, 1),
                coverUrl = "http://example.com/cover.jpg",
                totalPages = 440,
                price = 32000,
                category = "컴퓨터",
                tableOfContentsUrl = "http://example.com/toc",
                introductionUrl = "http://example.com/intro",
                summaryUrl = "http://example.com/summary",
            )

        // when
        val book = entity.asExternalModel()

        // then
        assertThat(book.isbn).isEqualTo(entity.isbn)
        assertThat(book.title).isEqualTo(entity.title)
        assertThat(book.publishedDate).isEqualTo(entity.publishedDate)
        assertThat(book.totalPages).isEqualTo(entity.totalPages)
    }

    @Test
    fun nlkBookDto_asExternalModel_mapsCorrectly() {
        // given
        val dto =
            NlkBookDto(
                isbn = "9788966263158",
                title = "소프트웨어 아키텍처 101",
                author = "마크 리처즈, 닐 포드",
                publisher = "한빛미디어",
                publishPredate = "20211101",
                titleUrl = "http://example.com/cover.jpg",
                page = "440",
                prePrice = "32000",
                subject = "컴퓨터",
                bookTbCntUrl = "http://example.com/toc",
                bookIntroductionUrl = "http://example.com/intro",
                bookSummaryUrl = "http://example.com/summary",
            )

        // when
        val book = dto.asExternalModel()

        // then
        assertThat(book.isbn).isEqualTo("9788966263158")
        assertThat(book.publishedDate).isEqualTo(LocalDate(2021, 11, 1))
        assertThat(book.totalPages).isEqualTo(440)
        assertThat(book.price).isEqualTo(32000)
        assertThat(book.coverUrl).isEqualTo("http://example.com/cover.jpg")
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
