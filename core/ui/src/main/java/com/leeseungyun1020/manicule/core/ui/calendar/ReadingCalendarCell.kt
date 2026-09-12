package com.leeseungyun1020.manicule.core.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeBorder
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.maniculeColors
import com.leeseungyun1020.manicule.core.designsystem.theme.size

@Composable
fun ReadingCalendarCell(
    intensity: Int?,
    modifier: Modifier = Modifier,
    isToday: Boolean = false,
    isSelected: Boolean = false,
) {
    val backgroundColor =
        if (intensity == null) {
            MaterialTheme.maniculeColors.calendarPlaceholder
        } else {
            val safeIntensity = intensity.coerceIn(0, 4)
            MaterialTheme.maniculeColors.calendarLevels[safeIntensity]
        }

    val todayRingColor = MaterialTheme.colorScheme.primary
    val selectedRingColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier =
            modifier
                .defaultMinSize(
                    minWidth = MaterialTheme.size.calendarCell,
                    minHeight = MaterialTheme.size.calendarCell,
                )
                .clip(MaterialTheme.shapes.extraSmall)
                .background(backgroundColor)
                .then(
                    if (isToday || isSelected) {
                        Modifier.border(
                            width = ManiculeBorder.ring,
                            color = if (isSelected) selectedRingColor else todayRingColor,
                            shape = MaterialTheme.shapes.extraSmall,
                        )
                    } else {
                        Modifier
                    },
                ),
    )
}

@ManiculePreview
@Composable
private fun ReadingCalendarCellPreview() {
    ManiculePreviewTheme {
        Box(modifier = Modifier.padding(ManiculeSpacing.lg)) {
            ReadingCalendarCell(intensity = 3, isToday = true)
        }
    }
}
