package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.bookdetail.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun StatusSelector(
    status: ReadingStatus?,
    onStatusSelected: (ReadingStatus) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val options = listOf(ReadingStatus.WANT, ReadingStatus.READING, ReadingStatus.FINISHED)
    val labels = listOf(
        stringResource(R.string.book_detail_status_want),
        stringResource(R.string.book_detail_status_reading),
        stringResource(R.string.book_detail_status_finished),
    )
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = status == option,
                onClick = { onStatusSelected(option) },
                enabled = enabled,
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
                modifier = Modifier.fillMaxHeight().heightIn(min = MaterialTheme.size.touchTargetMin),
                label = { Text(labels[index], textAlign = TextAlign.Center) },
            )
        }
    }
}

@ManiculePreview
@Composable
private fun StatusSelectorPreview() {
    ManiculePreviewTheme {
        Column {
            StatusSelector(status = null, onStatusSelected = {})
            ReadingStatus.entries.forEach { status ->
                StatusSelector(status = status, onStatusSelected = {})
                StatusSelector(status = status, onStatusSelected = {}, enabled = false)
            }
        }
    }
}
