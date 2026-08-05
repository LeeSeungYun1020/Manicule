package com.leeseungyun1020.manicule.core.ui.book

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.ui.preview.BookPreviewParameterProvider

@Composable
fun BookListItem(
    title: String,
    author: String,
    publisher: String,
    pubDate: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(MaterialTheme.spacing.lg),
    ) {
        BookCover(
            imageUrl = imageUrl,
            size = BookCoverSize.Small,
            contentDescription = title,
            showBorder = true,
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.lg))
        Column(
            modifier =
                Modifier
                    .height(BookCoverSize.Small.height)
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
            Text(
                text = "$publisher · $pubDate",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@ManiculePreview
@Composable
private fun BookListItemPreview(
    @PreviewParameter(BookPreviewParameterProvider::class) book: Book,
) {
    ManiculeTheme {
        BookListItem(
            title = book.title,
            author = book.author,
            publisher = book.publisher,
            pubDate = book.publishedDate?.toString().orEmpty(),
            imageUrl = book.coverUrl,
        )
    }
}
