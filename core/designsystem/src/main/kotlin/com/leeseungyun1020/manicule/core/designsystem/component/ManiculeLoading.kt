package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize

@Composable
fun ManiculeLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

private val LOADING_PREVIEW_CONTAINER_SIZE = 160.dp

@ManiculePreview
@Composable
private fun ManiculeLoadingFullSizePreview() {
    ManiculePreviewTheme {
        Box(modifier = Modifier.size(LOADING_PREVIEW_CONTAINER_SIZE)) {
            ManiculeLoading(modifier = Modifier.fillMaxSize())
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeLoadingPagingPreview() {
    ManiculePreviewTheme {
        ManiculeLoading(modifier = Modifier.size(ManiculeSize.touchTargetMin))
    }
}
