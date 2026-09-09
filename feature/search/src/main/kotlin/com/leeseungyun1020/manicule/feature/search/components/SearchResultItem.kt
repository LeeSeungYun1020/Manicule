package com.leeseungyun1020.manicule.feature.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.ui.book.BookListItem
import com.leeseungyun1020.manicule.core.ui.preview.BookPreviewParameterProvider

@Composable
internal fun SearchResultItem(
    book: Book,
    onBookSelected: (String) -> Unit,
    showDivider: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        BookListItem(
            title = book.title,
            author = book.author,
            publisher = book.publisher,
            pubDate = book.publishedDate?.toString().orEmpty(),
            imageUrl = book.coverUrl,
            modifier = Modifier
                .clickable(role = Role.Button) { onBookSelected(book.isbn) }
                .semantics(mergeDescendants = true) {},
        )
        if (showDivider) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@ManiculePreview
@Composable
private fun SearchResultItemPreview() {
    ManiculePreviewTheme {
        Column {
            val books = BookPreviewParameterProvider().values.toList()
            books.forEachIndexed { index, book ->
                SearchResultItem(book = book, onBookSelected = {}, showDivider = index < books.lastIndex)
            }
        }
    }
}
