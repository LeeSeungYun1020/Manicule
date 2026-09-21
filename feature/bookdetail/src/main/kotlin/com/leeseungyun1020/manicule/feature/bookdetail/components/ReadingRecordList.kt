package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeader
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.ui.book.BookProgressBar
import com.leeseungyun1020.manicule.feature.bookdetail.R
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private enum class ReadingRecordContentType {
    Title,
    Progress,
    Add,
    DateHeader,
    Session,
}

internal fun LazyListScope.readingRecordListItems(
    groupedRecords: List<Pair<LocalDate, List<ReadingRecord>>>,
    maxEndPage: Int,
    totalPages: Int?,
    onAddRecord: () -> Unit,
) {
    item(
        key = "reading-record-title",
        contentType = ReadingRecordContentType.Title,
    ) {
        ManiculeSectionHeader(
            title = stringResource(R.string.book_detail_records_title),
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.md),
        )
    }

    if (totalPages != null && totalPages > 0) {
        item(
            key = "reading-record-progress",
            contentType = ReadingRecordContentType.Progress,
        ) {
            BookProgressBar(
                currentPage = maxEndPage,
                totalPages = totalPages,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.md),
            )
        }
    }

    item(
        key = "reading-record-add",
        contentType = ReadingRecordContentType.Add,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.md),
            contentAlignment = Alignment.Center,
        ) {
            ManiculeButton(
                onClick = onAddRecord,
                text = stringResource(R.string.book_detail_add_record_button),
                leadingIcon = {
                    Icon(
                        imageVector = ManiculeIcons.Add,
                        contentDescription = null,
                    )
                },
            )
        }
    }

    readingRecordSessionItems(groupedRecords)
}

private fun LazyListScope.readingRecordSessionItems(groupedRecords: List<Pair<LocalDate, List<ReadingRecord>>>) {
    groupedRecords.forEach { (date, sessions) ->
        item(
            key = "reading-record-date-$date",
            contentType = ReadingRecordContentType.DateHeader,
        ) {
            ReadingRecordDateHeader(
                date = date,
                totalPagesForDate = sessions.sumOf { it.pagesRead },
            )
        }
        items(
            items = sessions,
            key = ReadingRecord::id,
            contentType = { ReadingRecordContentType.Session },
        ) { record ->
            Column {
                ReadingRecordSessionItem(record = record)
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                )
            }
        }
    }
}

@Composable
private fun ReadingRecordDateHeader(
    date: LocalDate,
    totalPagesForDate: Int,
    modifier: Modifier = Modifier,
) {
    val currentYear = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year }
    val dateText =
        if (date.year == currentYear) {
            stringResource(R.string.book_detail_date_format, date.monthNumber, date.dayOfMonth)
        } else {
            stringResource(R.string.book_detail_date_with_year_format, date.year, date.monthNumber, date.dayOfMonth)
        }

    Row(
        modifier = modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.md, bottom = MaterialTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = dateText,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.book_detail_record_pages_sum, totalPagesForDate),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ReadingRecordSessionItem(
    record: ReadingRecord,
    modifier: Modifier = Modifier,
) {
    val timeFormatRes = if (record.time.hour < 12) R.string.book_detail_time_am else R.string.book_detail_time_pm
    val displayHour = if (record.time.hour % 12 == 0) 12 else record.time.hour % 12
    val timeString = stringResource(timeFormatRes, displayHour, record.time.minute)

    Row(
        modifier = modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(
                R.string.book_detail_record_session_format,
                timeString,
                record.startPage,
                record.endPage,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row {
            ManiculeIconButton(
                onClick = {},
                enabled = false,
                icon = {
                    Icon(
                        imageVector = ManiculeIcons.Edit,
                        contentDescription = stringResource(R.string.book_detail_edit_record),
                    )
                },
            )
            ManiculeIconButton(
                onClick = {},
                enabled = false,
                icon = {
                    Icon(
                        imageVector = ManiculeIcons.Delete,
                        contentDescription = stringResource(R.string.book_detail_delete_record),
                    )
                },
            )
        }
    }
}

@ManiculePreview
@Preview(name = "Locale ko", locale = "ko")
@Composable
private fun ReadingRecordListPreview() {
    ManiculePreviewTheme {
        val records =
            listOf(
                ReadingRecord(1L, "123", LocalDate(2026, 7, 8), LocalTime(21, 12), 43, 68),
                ReadingRecord(2L, "123", LocalDate(2026, 7, 6), LocalTime(20, 3), 11, 42),
            )
        LazyColumn(contentPadding = MaterialTheme.spacing.screenContent) {
            readingRecordListItems(
                groupedRecords = records.groupBy { it.date }.toList().sortedByDescending { it.first },
                maxEndPage = records.maxOf { it.endPage },
                totalPages = 264,
                onAddRecord = {},
            )
        }
    }
}
