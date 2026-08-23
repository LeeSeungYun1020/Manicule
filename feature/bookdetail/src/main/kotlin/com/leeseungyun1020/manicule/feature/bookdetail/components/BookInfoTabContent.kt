package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.ui.book.BookCover
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize
import com.leeseungyun1020.manicule.feature.bookdetail.R
import java.text.NumberFormat

@Composable
fun BookInfoTabContent(
    book: Book,
    refreshFailed: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = MaterialTheme.spacing.screenContent,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sectionGap),
    ) {
        if (refreshFailed) {
            item(key = "refresh_error", contentType = "notice") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.book_detail_refresh_error),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.weight(1f),
                    )
                    ManiculeButton(onClick = onRetry, text = stringResource(R.string.book_detail_retry))
                }
            }
        }
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
