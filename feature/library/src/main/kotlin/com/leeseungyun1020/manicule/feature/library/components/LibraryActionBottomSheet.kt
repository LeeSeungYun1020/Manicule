package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeBottomSheet
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.ui.book.BookCover
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize
import com.leeseungyun1020.manicule.feature.library.R
import kotlinx.datetime.Instant

@Composable
fun LibraryActionBottomSheet(
    entry: BookEntry,
    onStatusSelected: (ReadingStatus) -> Unit,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManiculeBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        LibraryActionSheetContent(entry, onStatusSelected, onDelete, onDismissRequest)
    }
}

@Composable
private fun LibraryActionSheetContent(
    entry: BookEntry,
    onStatusSelected: (ReadingStatus) -> Unit,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(
                horizontal = MaterialTheme.spacing.lg,
                vertical = MaterialTheme.spacing.sm,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BookCover(
                imageUrl = entry.book.coverUrl,
                contentDescription = null,
                size = BookCoverSize.Small,
            )
            Text(
                text = entry.book.title,
                modifier = Modifier.weight(1f).padding(horizontal = MaterialTheme.spacing.md),
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            ManiculeIconButton(onClick = onDismissRequest) {
                Icon(ManiculeIcons.Close, contentDescription = stringResource(R.string.library_action_close))
            }
        }
        listOf(ReadingStatus.WANT, ReadingStatus.READING, ReadingStatus.FINISHED)
            .filter { it != entry.status }
            .forEach { status ->
                ListItem(
                    headlineContent = { Text(status.actionLabel()) },
                    leadingContent = { Icon(status.actionIcon(), contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().clickable { onStatusSelected(status) },
                )
            }
        ListItem(
            headlineContent = { Text(stringResource(R.string.library_action_delete)) },
            leadingContent = { Icon(ManiculeIcons.Delete, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().clickable(onClick = onDelete),
        )
    }
}

@Composable
private fun ReadingStatus.actionLabel(): String =
    when (this) {
        ReadingStatus.WANT -> stringResource(R.string.library_action_move_want)
        ReadingStatus.READING -> stringResource(R.string.library_action_move_reading)
        ReadingStatus.FINISHED -> stringResource(R.string.library_action_move_finished)
        ReadingStatus.UNSET -> error("UNSET is not a library action")
    }

private fun ReadingStatus.actionIcon(): ImageVector =
    when (this) {
        ReadingStatus.WANT -> ManiculeIcons.Bookmark
        ReadingStatus.READING -> ManiculeIcons.Tab.LibraryOutlined
        ReadingStatus.FINISHED -> ManiculeIcons.DoneAll
        ReadingStatus.UNSET -> error("UNSET is not a library action")
    }

@ManiculePreview
@Composable
private fun LibraryActionSheetPreview() {
    ManiculePreviewTheme {
        LibraryActionSheetContent(
            entry = BookEntry(
                book = com.leeseungyun1020.manicule.core.model.Book(
                    isbn = "9780000000001",
                    title = "책 제목",
                    author = "작가",
                    publisher = "출판사",
                    publishedDate = null,
                    coverUrl = null,
                    totalPages = null,
                    price = null,
                    category = null,
                    tableOfContentsUrl = null,
                    introductionUrl = null,
                    summaryUrl = null,
                ),
                status = ReadingStatus.WANT,
                addedAt = Instant.fromEpochMilliseconds(0),
                updatedAt = Instant.fromEpochMilliseconds(0),
            ),
            onStatusSelected = {},
            onDelete = {},
            onDismissRequest = {},
        )
    }
}
