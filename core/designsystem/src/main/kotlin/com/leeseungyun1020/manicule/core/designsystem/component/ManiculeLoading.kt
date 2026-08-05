package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme

@Composable
fun ManiculeLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@ManiculePreview
@Composable
private fun ManiculeLoadingFullSizePreview() {
    ManiculeTheme {
        Box(modifier = Modifier.size(ManiculeSize.chartHeight)) {
            ManiculeLoading(modifier = Modifier.fillMaxSize())
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeLoadingPagingPreview() {
    ManiculeTheme {
        ManiculeLoading(modifier = Modifier.size(ManiculeSize.touchTargetMin))
    }
}
