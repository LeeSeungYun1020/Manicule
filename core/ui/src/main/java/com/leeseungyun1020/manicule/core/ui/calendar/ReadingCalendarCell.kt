package com.leeseungyun1020.manicule.core.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeBorder
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.maniculeColors
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

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
                .drawBehind {
                    if (isToday || isSelected) {
                        val offsetPx = ManiculeSize.calendarTodayRingOffset.toPx()
                        val halfStroke = ManiculeBorder.ring.toPx() / 2f
                        val expand = offsetPx + halfStroke
                        drawRoundRect(
                            color = if (isSelected) selectedRingColor else todayRingColor,
                            topLeft = Offset(-expand, -expand),
                            size = Size(size.width + expand * 2, size.height + expand * 2),
                            cornerRadius = CornerRadius(MaterialTheme.spacing.xs.toPx() + expand),
                            style = Stroke(width = ManiculeBorder.ring.toPx()),
                        )
                    }
                }
                .clip(MaterialTheme.shapes.extraSmall)
                .background(backgroundColor),
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
