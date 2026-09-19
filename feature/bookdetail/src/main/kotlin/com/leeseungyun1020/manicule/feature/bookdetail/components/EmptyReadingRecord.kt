package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.feature.bookdetail.R

@Composable
internal fun EmptyReadingRecord(
    onAddRecord: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        Text(
            text = stringResource(R.string.book_detail_records_title),
            style = MaterialTheme.typography.titleMedium,
        )
        ManiculeEmptyState(
            title = stringResource(R.string.book_detail_records_empty_title),
            description = stringResource(R.string.book_detail_records_empty_description),
            icon = {
                Icon(
                    imageVector = ManiculeIcons.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outlineVariant,
                )
            },
            actions = {
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
            },
        )
    }
}

@ManiculePreview
@Composable
private fun EmptyReadingRecordPreview() {
    ManiculePreviewTheme {
        EmptyReadingRecord(onAddRecord = {})
    }
}
