package com.leeseungyun1020.manicule.core.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.ui.R
import com.leeseungyun1020.manicule.core.ui.preview.BookPreviewParameterProvider

@Composable
fun BookListItem(
    title: String,
    author: String,
    publisher: String,
    pubDate: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
    placeholder: Painter? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(MaterialTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCover(
            imageUrl = imageUrl,
            size = BookCoverSize.Small,
            contentDescription = title,
            showBorder = true,
            placeholder = placeholder,
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.lg))
        Column(
            modifier =
                Modifier
                    .heightIn(BookCoverSize.Small.height)
                    .weight(1f),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = author,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            listOfNotNull(
                publisher.takeIf { it.isNotBlank() },
                pubDate.takeIf { it.isNotBlank() },
            ).joinToString(" · ")
                .takeIf { it.isNotBlank() }
                ?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.md))
            trailingContent()
        }
    }
}

@ManiculePreview
@Composable
private fun BookListItemPreview() {
    val books = BookPreviewParameterProvider().values.toList()
    ManiculeTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            books.forEach { book ->
                BookListItem(
                    title = book.title,
                    author = book.author,
                    publisher = book.publisher,
                    pubDate = book.publishedDate?.toString().orEmpty(),
                    imageUrl = book.coverUrl,
                    placeholder = painterResource(id = R.drawable.sample_book_cover),
                )
            }
        }
    }
}

@ManiculePreview
@Composable
private fun BookListItemWithTrailingPreview() {
    val books = BookPreviewParameterProvider().values.toList()
    ManiculeTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            books.forEach { book ->
                BookListItem(
                    title = book.title,
                    author = book.author,
                    publisher = book.publisher,
                    pubDate = book.publishedDate?.toString().orEmpty(),
                    imageUrl = book.coverUrl,
                    placeholder = painterResource(id = R.drawable.sample_book_cover),
                    trailingContent = {
                        Text(
                            text = "${book.totalPages?.let { it / 4 } ?: 300}p",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            }
        }
    }
}
