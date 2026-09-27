package com.leeseungyun1020.manicule.feature.stats.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.leeseungyun1020.manicule.core.domain.stats.ReadingDayBook
import com.leeseungyun1020.manicule.core.ui.book.BookListItem
import com.leeseungyun1020.manicule.feature.stats.R

@Composable
fun ReadingDayBookItem(
    book: ReadingDayBook,
    onBookSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val meta = book.book
    BookListItem(
        title = meta?.title ?: stringResource(R.string.stats_book_missing),
        author = meta?.author.orEmpty(),
        publisher = meta?.publisher.orEmpty(),
        pubDate = meta?.publishedDate?.toString().orEmpty(),
        imageUrl = meta?.coverUrl,
        modifier = modifier.clickable(role = Role.Button) { onBookSelected(book.isbn) },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = pluralStringResource(R.plurals.stats_pages_value, book.pagesRead, book.pagesRead),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = pluralStringResource(R.plurals.stats_session_count, book.recordCount, book.recordCount),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
