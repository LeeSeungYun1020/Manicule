package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

@Composable
internal fun ReadingRecordList(
    records: List<ReadingRecord>,
    totalPages: Int?,
    onAddRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxEndPage = remember(records) { records.maxOfOrNull { it.endPage } ?: 0 }
    val groupedRecords = remember(records) {
        records.groupBy { it.date }.toList().sortedByDescending { it.first }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        ManiculeSectionHeader(title = stringResource(R.string.book_detail_records_title))

        if (totalPages != null && totalPages > 0) {
            BookProgressBar(
                currentPage = maxEndPage,
                totalPages = totalPages,
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
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

        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            groupedRecords.forEach { (date, sessions) ->
                ReadingRecordDateGroup(
                    date = date,
                    sessions = sessions.sortedByDescending { it.time },
                )
            }
        }
    }
}

@Composable
private fun ReadingRecordDateGroup(
    date: LocalDate,
    sessions: List<ReadingRecord>,
    modifier: Modifier = Modifier,
) {
    val totalPagesForDate = remember(sessions) { sessions.sumOf { it.pagesRead } }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = MaterialTheme.spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.book_detail_date_format, date.monthNumber, date.dayOfMonth),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(R.string.book_detail_record_pages_sum, totalPagesForDate),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        sessions.forEach { record ->
            ReadingRecordSessionItem(record = record)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
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
@Composable
private fun ReadingRecordListPreview() {
    ManiculePreviewTheme {
        ReadingRecordList(
            records = listOf(
                ReadingRecord(1L, "123", LocalDate(2026, 7, 8), LocalTime(21, 12), 43, 68),
                ReadingRecord(2L, "123", LocalDate(2026, 7, 6), LocalTime(20, 3), 11, 42),
            ),
            totalPages = 264,
            onAddRecord = {},
        )
    }
}
