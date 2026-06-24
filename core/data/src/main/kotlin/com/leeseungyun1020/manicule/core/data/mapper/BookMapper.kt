package com.leeseungyun1020.manicule.core.data.mapper

import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkBookDto
import kotlinx.datetime.LocalDate

fun BookEntity.asExternalModel() =
    Book(
        isbn = isbn,
        title = title,
        author = author,
        publisher = publisher,
        publishedDate = publishedDate,
        coverUrl = coverUrl,
        totalPages = totalPages,
        price = price,
        category = category,
        tableOfContentsUrl = tableOfContentsUrl,
        introductionUrl = introductionUrl,
        summaryUrl = summaryUrl,
    )

fun NlkBookDto.asExternalModel(): Book =
    Book(
        isbn = isbn,
        title = title,
        author = author,
        publisher = publisher,
        publishedDate = parseNlkDate(publishPredate),
        coverUrl = titleUrl.ifBlank { null },
        totalPages = page.toIntOrNull(),
        price = prePrice.toIntOrNull(),
        category = subject.ifBlank { null },
        tableOfContentsUrl = bookTbCntUrl.ifBlank { null },
        introductionUrl = bookIntroductionUrl.ifBlank { null },
        summaryUrl = bookSummaryUrl.ifBlank { null },
    )

internal fun parseNlkDate(dateString: String?): LocalDate? {
    if (dateString == null || dateString.length != 8) return null
    return try {
        val year = dateString.substring(0, 4).toInt()
        val month = dateString.substring(4, 6).toInt()
        val day = dateString.substring(6, 8).toInt()
        if (year in 1000..9999 && month in 1..12 && day in 1..31) {
            LocalDate(year, month, day)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
