package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeDashedCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTextField
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.bookdetail.R
import com.leeseungyun1020.manicule.feature.bookdetail.RecordLoadState

private enum class MyRecordContentType {
    Status,
    Review,
    EmptyRecords,
    RecordError,
}

@Composable
internal fun MyRecordTabContent(
    status: ReadingStatus?,
    isSaving: Boolean,
    rating: Int,
    memo: String?,
    memoDraft: String? = null,
    isRatingSaving: Boolean = false,
    isMemoSaving: Boolean = false,
    records: List<ReadingRecord>,
    recordLoadState: RecordLoadState,
    totalPages: Int?,
    onStatusSelected: (ReadingStatus) -> Unit,
    onRatingSelected: (Int) -> Unit,
    onMemoDraftChanged: (String) -> Unit = {},
    onSaveMemo: () -> Unit = {},
    onAddRecord: () -> Unit,
    onRetryRecords: () -> Unit,
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

        item(
            key = "book-detail-review",
            contentType = MyRecordContentType.Review,
        ) {
            Column(
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            ) {
                Text(
                    text = stringResource(R.string.book_detail_review_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                BookDetailReviewCard(
                    rating = rating,
                    memo = memo,
                    memoDraft = memoDraft,
                    isRatingSaving = isRatingSaving,
                    isMemoSaving = isMemoSaving,
                    onRatingSelected = onRatingSelected,
                    onMemoDraftChanged = onMemoDraftChanged,
                    onSaveMemo = onSaveMemo,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (records.isEmpty() && recordLoadState is RecordLoadState.Failed) {
            item(
                key = "reading-records-error",
                contentType = MyRecordContentType.RecordError,
            ) {
                ReadingRecordLoadError(onRetry = onRetryRecords)
            }
        } else if (records.isEmpty()) {
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

@Composable
internal fun BookDetailReviewCard(
    rating: Int,
    memo: String?,
    memoDraft: String?,
    isRatingSaving: Boolean,
    isMemoSaving: Boolean,
    onRatingSelected: (Int) -> Unit,
    onMemoDraftChanged: (String) -> Unit,
    onSaveMemo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentMemo = memoDraft ?: memo.orEmpty()
    val isReviewEmpty = rating == 0 && currentMemo.isBlank()
    var hasHadFocus by rememberSaveable { mutableStateOf(false) }

    val cardContent: @Composable ColumnScope.() -> Unit = {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.md),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            BookDetailRatingBar(
                rating = rating,
                isSaving = isRatingSaving,
                onRatingSelected = onRatingSelected,
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            val memoLabel = stringResource(R.string.book_detail_memo_label)
            ManiculeTextField(
                value = currentMemo,
                onValueChange = onMemoDraftChanged,
                placeholder = stringResource(R.string.book_detail_rating_empty_prompt),
                enabled = !isMemoSaving,
                singleLine = false,
                maxLines = 5,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .semantics {
                            contentDescription = memoLabel
                        }
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused) {
                                hasHadFocus = true
                            } else if (hasHadFocus) {
                                onSaveMemo()
                            }
                        },
            )
            if (isMemoSaving) {
                Text(
                    text = stringResource(R.string.book_detail_memo_saving),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (isReviewEmpty) {
        ManiculeDashedCard(
            modifier = modifier,
            horizontalAlignment = Alignment.Start,
            content = cardContent,
        )
    } else {
        ManiculeCard(
            modifier = modifier,
            content = cardContent,
        )
    }
}

@ManiculePreview
@Composable
private fun MyRecordUnregisteredEmptyRatingPreview() {
    ManiculePreviewTheme {
        MyRecordTabContent(
            status = null,
            isSaving = false,
            rating = 0,
            memo = null,
            isRatingSaving = false,
            records = emptyList(),
            recordLoadState = RecordLoadState.Idle,
            totalPages = null,
            onStatusSelected = {},
            onRatingSelected = {},
            onAddRecord = {},
            onRetryRecords = {},
        )
    }
}

@ManiculePreview
@Composable
private fun MyRecordRatedWithMemoPreview() {
    ManiculePreviewTheme {
        MyRecordTabContent(
            status = ReadingStatus.READING,
            isSaving = false,
            rating = 4,
            memo = "담담한 문장이 오래 남는 책. 다시 읽고 싶다.",
            isRatingSaving = false,
            records = emptyList(),
            recordLoadState = RecordLoadState.Idle,
            totalPages = 264,
            onStatusSelected = {},
            onRatingSelected = {},
            onAddRecord = {},
            onRetryRecords = {},
        )
    }
}

@ManiculePreview
@Composable
private fun MyRecordRatingSavingPreview() {
    ManiculePreviewTheme {
        MyRecordTabContent(
            status = ReadingStatus.READING,
            isSaving = false,
            rating = 4,
            memo = null,
            isRatingSaving = true,
            records = emptyList(),
            recordLoadState = RecordLoadState.Idle,
            totalPages = 264,
            onStatusSelected = {},
            onRatingSelected = {},
            onAddRecord = {},
            onRetryRecords = {},
        )
    }
}

@ManiculePreview
@Composable
private fun MyRecordReviewOnlyPreview() {
    ManiculePreviewTheme {
        MyRecordTabContent(
            status = ReadingStatus.UNSET,
            isSaving = false,
            rating = 5,
            memo = null,
            isRatingSaving = false,
            records = emptyList(),
            recordLoadState = RecordLoadState.Idle,
            totalPages = null,
            onStatusSelected = {},
            onRatingSelected = {},
            onAddRecord = {},
            onRetryRecords = {},
        )
    }
}

@ManiculePreview
@Composable
private fun MyRecordMemoEditingPreview() {
    ManiculePreviewTheme {
        MyRecordTabContent(
            status = ReadingStatus.READING,
            isSaving = false,
            rating = 4,
            memo = "이전 메모",
            memoDraft = "수정 중인 메모 초안",
            isRatingSaving = false,
            isMemoSaving = false,
            records = emptyList(),
            recordLoadState = RecordLoadState.Idle,
            totalPages = 264,
            onStatusSelected = {},
            onRatingSelected = {},
            onAddRecord = {},
            onRetryRecords = {},
        )
    }
}

@ManiculePreview
@Composable
private fun MyRecordMemoSavingPreview() {
    ManiculePreviewTheme {
        MyRecordTabContent(
            status = ReadingStatus.READING,
            isSaving = false,
            rating = 4,
            memo = "저장 중인 메모",
            isRatingSaving = false,
            isMemoSaving = true,
            records = emptyList(),
            recordLoadState = RecordLoadState.Idle,
            totalPages = 264,
            onStatusSelected = {},
            onRatingSelected = {},
            onAddRecord = {},
            onRetryRecords = {},
        )
    }
}
