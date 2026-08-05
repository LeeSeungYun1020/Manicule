package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme

@Composable
fun ManiculeSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (actionLabel != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(text = actionLabel)
            }
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeSectionHeaderPreview() {
    ManiculeTheme {
        ManiculeSectionHeader(
            title = "Recently searched",
            modifier = Modifier.padding(horizontal = ManiculeSpacing.lg),
            actionLabel = "Clear all",
            onActionClick = {},
        )
    }
}

@ManiculePreview
@Composable
private fun ManiculeSectionHeaderWithoutActionPreview() {
    ManiculeTheme {
        ManiculeSectionHeader(
            title = "Settings",
            modifier = Modifier.padding(horizontal = ManiculeSpacing.lg),
        )
    }
}
