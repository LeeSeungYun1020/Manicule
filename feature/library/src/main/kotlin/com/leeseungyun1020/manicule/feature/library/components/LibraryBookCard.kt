package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.ui.book.BookCover
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize
import com.leeseungyun1020.manicule.feature.library.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

@Composable
fun LibraryBookCard(
    entry: BookEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val longClickLabel = stringResource(R.string.library_action_open)
    val totalPages = entry.book.totalPages
    val progress =
        if (totalPages != null && totalPages > 0) {
            val currentPage = entry.currentPage?.coerceAtLeast(0) ?: 0
            (currentPage.toFloat() / totalPages).coerceIn(0f, 1f)
        } else {
            null
        }

    val overlayText =
        when (entry.status) {
            ReadingStatus.READING ->
                progress?.let {
                    stringResource(R.string.library_book_progress, (it * 100).roundToInt())
                }
            ReadingStatus.FINISHED ->
                entry.finishedAt?.let {
                    stringResource(
                        R.string.library_book_finished_date,
                        it.year,
                        it.monthNumber,
                        it.dayOfMonth,
                    )
                }
            ReadingStatus.WANT, ReadingStatus.UNSET -> null
        }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClickLabel = longClickLabel,
                    onLongClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    },
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier =
                Modifier
                    .size(BookCoverSize.Medium.width, BookCoverSize.Medium.height)
                    .clip(MaterialTheme.shapes.extraSmall),
        ) {
            BookCover(
                imageUrl = entry.book.coverUrl,
                contentDescription = null,
                size = BookCoverSize.Medium,
                showBorder = true,
            )
            if (entry.status == ReadingStatus.READING && progress != null && progress > 0f) {
                BookmarkRibbon(
                    progress = progress,
                    modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = MaterialTheme.spacing.sm),
                )
            }
            if (overlayText != null) {
                BookCoverStatusOverlay(
                    text = overlayText,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
        Text(
            text = entry.book.title,
            modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@ManiculePreview
@Composable
private fun LibraryBookCardReadingPreview() {
    ManiculePreviewTheme {
        LibraryBookCard(
            entry =
                BookEntry(
                    book =
                        Book(
                            isbn = "9780000000001",
                            title = "책 제목",
                            author = "작가",
                            publisher = "출판사",
                            publishedDate = null,
                            coverUrl = null,
                            totalPages = 300,
                            price = null,
                            category = null,
                            tableOfContentsUrl = null,
                            introductionUrl = null,
                            summaryUrl = null,
                        ),
                    status = ReadingStatus.READING,
                    addedAt = Instant.fromEpochMilliseconds(0),
                    updatedAt = Instant.fromEpochMilliseconds(0),
                    currentPage = 192,
                ),
            onClick = {},
            onLongClick = {},
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
        )
    }
}

@ManiculePreview
@Composable
private fun LibraryBookCardFinishedPreview() {
    ManiculePreviewTheme {
        LibraryBookCard(
            entry =
                BookEntry(
                    book =
                        Book(
                            isbn = "9780000000002",
                            title = "다 읽은 책 제목",
                            author = "작가",
                            publisher = "출판사",
                            publishedDate = null,
                            coverUrl = null,
                            totalPages = 300,
                            price = null,
                            category = null,
                            tableOfContentsUrl = null,
                            introductionUrl = null,
                            summaryUrl = null,
                        ),
                    status = ReadingStatus.FINISHED,
                    addedAt = Instant.fromEpochMilliseconds(0),
                    updatedAt = Instant.fromEpochMilliseconds(0),
                    finishedAt = LocalDate(2026, 7, 8),
                ),
            onClick = {},
            onLongClick = {},
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
        )
    }
}

@ManiculePreview
@Composable
private fun LibraryBookCardWantPreview() {
    ManiculePreviewTheme {
        LibraryBookCard(
            entry =
                BookEntry(
                    book =
                        Book(
                            isbn = "9780000000003",
                            title = "읽고 싶은 책 제목",
                            author = "작가",
                            publisher = "출판사",
                            publishedDate = null,
                            coverUrl = null,
                            totalPages = 300,
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
            onClick = {},
            onLongClick = {},
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
        )
    }
}
