package com.leeseungyun1020.manicule.core.ui.contribution

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.theme.LocalGrassColors
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme

@Composable
fun ContributionCell(
    intensity: Int?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val grassColors = LocalGrassColors.current
    val backgroundColor =
        if (intensity == null) {
            Color.Transparent
        } else {
            val safeIntensity = intensity.coerceIn(0, 4)
            grassColors[safeIntensity]
        }

    val clickableModifier =
        if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .size(12.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(backgroundColor)
                .then(clickableModifier),
    )
}

@ManiculePreview
@Composable
private fun ContributionCellPreview() {
    ManiculeTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ContributionCell(intensity = 3)
        }
    }
}
