package com.leeseungyun1020.manicule.core.ui.contribution

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.theme.GrassDark
import com.leeseungyun1020.manicule.core.designsystem.theme.GrassLight

@Composable
fun ContributionCell(
    intensity: Int?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor =
        if (intensity == null) {
            Color.Transparent
        } else {
            val safeIntensity = intensity.coerceIn(0, 4)
            val colorList = if (isDarkTheme) GrassDark else GrassLight
            colorList[safeIntensity]
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

@Preview(showBackground = true)
@Composable
private fun ContributionCellPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            ContributionCell(intensity = 3)
        }
    }
}
