package com.leeseungyun1020.manicule.core.data.mapper

import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkBookDto
import kotlinx.datetime.LocalDate

private const val HTTP_PREFIX = "http://"
private const val HTTPS_PREFIX = "https://"
private const val NLK_HOST = "nl.go.kr"

fun BookEntity.asExternalModel() =
    Book(
        isbn = isbn,
        title = title,
        author = author,
        publisher = publisher,
        publishedDate = publishedDate,
        coverUrl = normalizeCoverUrl(coverUrl),
        totalPages = totalPages,
        price = price,
        category = category,
        tableOfContentsUrl = tableOfContentsUrl,
        introductionUrl = introductionUrl,
        summaryUrl = summaryUrl,
        introduction = introduction,
        tableOfContents = tableOfContents,
    )

fun NlkBookDto.asExternalModel(): Book =
    Book(
        isbn = isbn,
        title = title,
        author = author,
        publisher = publisher,
        publishedDate = parseNlkDate(publishPredate),
        coverUrl = normalizeCoverUrl(titleUrl),
        totalPages = page.filter { it.isDigit() }.toIntOrNull(),
        price = prePrice.filter { it.isDigit() }.toIntOrNull(),
        category = subject.ifBlank { null },
        tableOfContentsUrl = bookTbCntUrl.ifBlank { null },
        introductionUrl = bookIntroductionUrl.ifBlank { null },
        summaryUrl = bookSummaryUrl.ifBlank { null },
        introduction = bookIntroduction.ifBlank { bookSummary }.ifBlank { null },
        tableOfContents = bookTbCnt.ifBlank { null },
    )

internal fun NlkBookDto.asExternalModelOrNull(): Book? =
    takeIf { it.isbn.isNotBlank() && it.title.isNotBlank() }
        ?.asExternalModel()

internal fun normalizeCoverUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val trimmed = url.trim()
    if (trimmed.startsWith(HTTP_PREFIX, ignoreCase = true)) {
        val withoutScheme = trimmed.substring(HTTP_PREFIX.length)
        val host = withoutScheme.substringBefore('/').substringBefore(':')
        if (host.equals(NLK_HOST, ignoreCase = true) || host.endsWith(".$NLK_HOST", ignoreCase = true)) {
            return HTTPS_PREFIX + withoutScheme
        }
    }
    return trimmed
}

internal fun parseNlkDate(dateString: String): LocalDate? {
    // 국립중앙도서관 날짜 형식은 고정 길이 YYYYMMDD이다.
    if (dateString.length != 8) return null
    return runCatching {
        val year = dateString.substring(0, 4).toInt()
        val month = dateString.substring(4, 6).toInt()
        val day = dateString.substring(6, 8).toInt()
        LocalDate(year, month, day)
    }.getOrNull()
}

fun Book.asEntity() =
    BookEntity(
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
        introduction = introduction,
        tableOfContents = tableOfContents,
    )
