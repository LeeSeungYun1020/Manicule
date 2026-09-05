package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.bookdetail.R

@Composable
internal fun MyRecordTabContent(
    status: ReadingStatus?,
    isSaving: Boolean,
    onStatusSelected: (ReadingStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(MaterialTheme.spacing.screenContent),
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

@ManiculePreview
@Composable
private fun MyRecordUnregisteredPreview() {
    ManiculePreviewTheme { MyRecordTabContent(null, false, {}) }
}

@ManiculePreview
@Composable
private fun MyRecordReviewOnlyPreview() {
    ManiculePreviewTheme { MyRecordTabContent(ReadingStatus.UNSET, false, {}) }
}

@ManiculePreview
@Composable
private fun MyRecordSavingPreview() {
    ManiculePreviewTheme { MyRecordTabContent(ReadingStatus.READING, true, {}) }
}

@ManiculePreview
@Composable
private fun MyRecordRegisteredPreview() {
    ManiculePreviewTheme { MyRecordTabContent(ReadingStatus.FINISHED, false, {}) }
}
