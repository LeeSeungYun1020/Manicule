package com.leeseungyun1020.manicule.feature.search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.paging.LoadState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTextButton
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.feature.search.R

@Composable
internal fun SearchAppendState(
    loadState: LoadState,
    onRetry: () -> Unit,
) {
    when (loadState) {
        is LoadState.Loading -> {
            val description = stringResource(R.string.search_result_loading_more)
            ManiculeLoading(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(ManiculeSize.touchTargetMin)
                        .semantics { contentDescription = description },
            )
        }

        is LoadState.Error -> {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.search_result_append_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                ManiculeTextButton(
                    onClick = onRetry,
                    text = stringResource(R.string.search_retry),
                )
            }
        }

        is LoadState.NotLoading -> Unit
    }
}

@ManiculePreview
@Composable
private fun SearchAppendLoadingPreview() {
    ManiculePreviewTheme { SearchAppendState(loadState = LoadState.Loading, onRetry = {}) }
}

@ManiculePreview
@Composable
private fun SearchAppendErrorPreview() {
    ManiculePreviewTheme {
        SearchAppendState(loadState = LoadState.Error(IllegalStateException("Preview append failure")), onRetry = {})
    }
}
