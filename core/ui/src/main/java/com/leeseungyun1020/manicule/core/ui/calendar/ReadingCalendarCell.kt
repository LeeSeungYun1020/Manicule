package com.leeseungyun1020.manicule.core.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeBorder
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.maniculeColors
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

@Composable
fun ReadingCalendarCell(
    intensity: Int?,
    modifier: Modifier = Modifier,
    isToday: Boolean = false,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val backgroundColor =
        if (intensity == null) {
            MaterialTheme.maniculeColors.calendarPlaceholder
        } else {
            val safeIntensity = intensity.coerceIn(0, 4)
            MaterialTheme.maniculeColors.calendarLevels[safeIntensity]
        }

    val semanticsModifier =
        if (contentDescription != null) {
            Modifier.semantics {
                this.contentDescription = contentDescription
            }
        } else {
            Modifier
        }

    val clickableModifier =
        if (onClick != null) {
            Modifier.clickable(onClick = onClick)
        } else {
            Modifier
        }

    val todayRingColor = MaterialTheme.maniculeColors.calendarTodayRing

    Box(
        modifier =
            modifier
                .size(MaterialTheme.size.calendarCell)
                .drawBehind {
                    if (isToday) {
                        val offsetPx = ManiculeSize.calendarTodayRingOffset.toPx()
                        val halfStroke = ManiculeBorder.ring.toPx() / 2f
                        val expand = offsetPx + halfStroke
                        drawRoundRect(
                            color = todayRingColor,
                            topLeft = Offset(-expand, -expand),
                            size = Size(size.width + expand * 2, size.height + expand * 2),
                            cornerRadius = CornerRadius(MaterialTheme.spacing.xs.toPx() + expand),
                            style = Stroke(width = ManiculeBorder.ring.toPx()),
                        )
                    }
                }
                .clip(MaterialTheme.shapes.extraSmall)
                .background(backgroundColor)
                .then(clickableModifier)
                .then(semanticsModifier),
    )
}

@ManiculePreview
@Composable
private fun ReadingCalendarCellPreview() {
    ManiculeTheme {
        Box(modifier = Modifier.padding(ManiculeSpacing.lg)) {
            ReadingCalendarCell(intensity = 3, isToday = true)
        }
    }
}
