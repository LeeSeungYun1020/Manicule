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
        totalPages = page.filter { it.isDigit() }.toIntOrNull(),
        price = prePrice.filter { it.isDigit() }.toIntOrNull(),
        category = subject.ifBlank { null },
        tableOfContentsUrl = bookTbCntUrl.ifBlank { null },
        introductionUrl = bookIntroductionUrl.ifBlank { null },
        summaryUrl = bookSummaryUrl.ifBlank { null },
    )

internal fun parseNlkDate(dateString: String): LocalDate? {
    if (dateString.length != 8) return null
    return runCatching {
        val year = dateString.substring(0, 4).toInt()
        val month = dateString.substring(4, 6).toInt()
        val day = dateString.substring(6, 8).toInt()
        LocalDate(year, month, day)
    }.getOrNull()
}
