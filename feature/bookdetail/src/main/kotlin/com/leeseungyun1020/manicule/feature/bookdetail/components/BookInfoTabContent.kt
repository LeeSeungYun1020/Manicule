package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.ui.book.BookCover
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize
import com.leeseungyun1020.manicule.feature.bookdetail.R
import kotlinx.datetime.LocalDate
import java.text.NumberFormat

@Composable
fun BookInfoTabContent(
    book: Book,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = MaterialTheme.spacing.screenContent,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sectionGap),
    ) {
        item(key = "header", contentType = "header") { BookHeader(book) }
        item(key = "publication", contentType = "section") { BookPublicationInfo(book) }
        item(key = "introduction", contentType = "section") {
            BookDetailExpandableText(
                title = stringResource(R.string.book_detail_introduction),
                text = book.introduction,
            )
        }
        item(key = "contents", contentType = "section") {
            BookDetailExpandableText(
                title = stringResource(R.string.book_detail_contents),
                text = book.tableOfContents,
            )
        }
    }
}

@Composable
private fun BookHeader(book: Book) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        BookCover(
            imageUrl = book.coverUrl,
            contentDescription = stringResource(R.string.book_detail_cover_description, book.title),
            showBorder = true,
            size = BookCoverSize.Medium,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            Text(text = book.title, style = MaterialTheme.typography.titleLarge)
            Text(text = book.author, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = book.publisher,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BookPublicationInfo(book: Book) {
    val priceFormatter = remember { NumberFormat.getIntegerInstance() }
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        Text(text = stringResource(R.string.book_detail_publication_info), style = MaterialTheme.typography.titleMedium)
        PublicationRow(stringResource(R.string.book_detail_isbn), book.isbn)
        PublicationRow(stringResource(R.string.book_detail_published_date), book.publishedDate?.toString())
        PublicationRow(
            stringResource(R.string.book_detail_pages),
            book.totalPages?.let {
                stringResource(R.string.book_detail_pages_value, it)
            },
        )
        PublicationRow(
            stringResource(R.string.book_detail_price),
            book.price?.let { stringResource(R.string.book_detail_price_value, priceFormatter.format(it)) },
        )
        PublicationRow(stringResource(R.string.book_detail_category), book.category)
    }
}

@Composable
private fun PublicationRow(
    label: String,
    value: String?,
) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(2f))
    }
}

private val sampleBook =
    Book(
        isbn = "9791161759692",
        title = "Kotlin in Action 2/e",
        author = "세바스티안 아이그너 외",
        publisher = "에이콘출판사",
        publishedDate = LocalDate(2025, 2, 27),
        coverUrl = null,
        totalPages = 803,
        price = 48_000,
        category = "프로그래밍",
        tableOfContentsUrl = null,
        introductionUrl = null,
        summaryUrl = null,
        introduction = "코틀린 언어와 실전 개발 패턴을 소개합니다. ".repeat(8),
        tableOfContents = "1장 코틀린이란 무엇이며 왜 필요한가\n2장 코틀린 기초",
    )

@ManiculePreview
@Composable
private fun BookInfoTabContentPreview() {
    ManiculePreviewTheme {
        BookInfoTabContent(
            book = sampleBook,
        )
    }
}

@ManiculePreview
@Composable
private fun BookHeaderPreview() {
    ManiculePreviewTheme {
        BookHeader(book = sampleBook)
    }
}

@ManiculePreview
@Composable
private fun BookPublicationInfoPreview() {
    ManiculePreviewTheme {
        BookPublicationInfo(book = sampleBook)
    }
}

@ManiculePreview
@Composable
private fun PublicationRowPreview() {
    ManiculePreviewTheme {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            PublicationRow(label = "ISBN", value = "9791161759692")
            PublicationRow(label = "출간일", value = "2025-02-27")
            PublicationRow(label = "긴 설명 항목", value = "두 줄 이상으로 길게 이어지는 상세 정보 예시 텍스트입니다.")
        }
    }
}
