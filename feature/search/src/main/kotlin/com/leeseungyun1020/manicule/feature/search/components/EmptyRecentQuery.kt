package com.leeseungyun1020.manicule.feature.search.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.feature.search.R

@Composable
fun EmptyRecentQuery(modifier: Modifier = Modifier) {
    ManiculeEmptyState(
        title = stringResource(R.string.search_empty_title),
        description = stringResource(R.string.search_empty_description),
        modifier = modifier.fillMaxSize(),
        icon = {
            Icon(
                imageVector = ManiculeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(MaterialTheme.size.iconEmptyState),
            )
        },
    )
}

@ManiculePreview
@Composable
private fun EmptyRecentQueryPreview() {
    ManiculePreviewTheme {
        EmptyRecentQuery()
    }
}
