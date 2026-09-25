package com.leeseungyun1020.manicule.feature.stats.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarGrid
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarLegend
import com.leeseungyun1020.manicule.feature.stats.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val weekdayLabels = listOf(
    R.string.stats_monday,
    R.string.stats_tuesday,
    R.string.stats_wednesday,
    R.string.stats_thursday,
    R.string.stats_friday,
    R.string.stats_saturday,
    R.string.stats_sunday,
)

@Composable
fun StatsCalendarCard(
    days: List<ReadingCalendarDay>,
    today: LocalDate,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    ManiculeCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Text(stringResource(R.string.stats_calendar_title), style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.size.calendarCellGap)) {
                    weekdayLabels.forEach { label ->
                        Box(modifier = Modifier.size(MaterialTheme.size.calendarCell), contentAlignment = Alignment.Center) {
                            Text(
                                text = stringResource(label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                    }
                }
                ReadingCalendarGrid(
                    days = days,
                    today = today,
                    selectedDate = selectedDate,
                    isDateSelectable = { it.pages > 0 },
                    onDateSelected = onDateSelected,
                    modifier = Modifier.weight(1f),
                )
            }
            ReadingCalendarLegend(modifier = Modifier.align(Alignment.End))
        }
    }
}

@ManiculePreview
@Composable
private fun StatsCalendarCardPreview() {
    val today = LocalDate(2026, 9, 24)
    ManiculePreviewTheme {
        StatsCalendarCard(
            days = (1..28).map {
                ReadingCalendarDay.of(
                    LocalDate(2026, 8, 27).plus(DatePeriod(days = it - 1)),
                    if (it % 4 == 0) 25 else 0,
                )
            },
            today = today,
            selectedDate = null,
            onDateSelected = {},
        )
    }
}
