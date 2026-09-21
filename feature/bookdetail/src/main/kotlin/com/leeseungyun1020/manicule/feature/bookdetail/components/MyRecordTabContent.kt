package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.bookdetail.R

private enum class MyRecordContentType {
    Status,
    EmptyRecords,
}

@Composable
internal fun MyRecordTabContent(
    status: ReadingStatus?,
    isSaving: Boolean,
    records: List<ReadingRecord>,
    totalPages: Int?,
    onStatusSelected: (ReadingStatus) -> Unit,
    onAddRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val maxEndPage = remember(records) { records.maxOfOrNull { it.endPage } ?: 0 }
    val groupedRecords = remember(records) {
        records
            .groupBy { it.date }
            .map { (date, sessions) -> date to sessions.sortedByDescending { it.time } }
            .sortedByDescending { it.first }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = MaterialTheme.spacing.screenContent,
    ) {
        item(
            key = "reading-status",
            contentType = MyRecordContentType.Status,
        ) {
            Column(
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                Text(stringResource(R.string.book_detail_status_title), style = MaterialTheme.typography.titleMedium)
                StatusSelector(status = status, onStatusSelected = onStatusSelected, enabled = !isSaving)
                if (isSaving) {
                    Text(
                        stringResource(R.string.book_detail_status_saving),
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else if (status == null || status == ReadingStatus.UNSET) {
                    Text(
                        stringResource(R.string.book_detail_status_unset),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        if (records.isEmpty()) {
            item(
                key = "empty-reading-records",
                contentType = MyRecordContentType.EmptyRecords,
            ) {
                EmptyReadingRecord(onAddRecord = onAddRecord)
            }
        } else {
            readingRecordListItems(
                groupedRecords = groupedRecords,
                maxEndPage = maxEndPage,
                totalPages = totalPages,
                onAddRecord = onAddRecord,
            )
        }
    }
}

@ManiculePreview
@Composable
private fun MyRecordUnregisteredPreview() {
    ManiculePreviewTheme { MyRecordTabContent(null, false, emptyList(), null, {}, {}) }
}

@ManiculePreview
@Composable
private fun MyRecordReviewOnlyPreview() {
    ManiculePreviewTheme { MyRecordTabContent(ReadingStatus.UNSET, false, emptyList(), null, {}, {}) }
}

@ManiculePreview
@Composable
private fun MyRecordSavingPreview() {
    ManiculePreviewTheme { MyRecordTabContent(ReadingStatus.READING, true, emptyList(), null, {}, {}) }
}

@ManiculePreview
@Composable
private fun MyRecordRegisteredPreview() {
    ManiculePreviewTheme { MyRecordTabContent(ReadingStatus.FINISHED, false, emptyList(), null, {}, {}) }
}
