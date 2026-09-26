package com.leeseungyun1020.manicule.feature.stats.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.DpSize
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarCell
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarGrid
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarLegend
import com.leeseungyun1020.manicule.feature.stats.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import com.leeseungyun1020.manicule.core.ui.R as CoreUiR

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
    isTodayPeriod: Boolean = false,
) {
    ManiculeCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Text(stringResource(R.string.stats_calendar_title), style = MaterialTheme.typography.titleMedium)
            if (isTodayPeriod) {
                TodayCalendarStrip(
                    days = days,
                    today = today,
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                )
            } else {
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
            }
            ReadingCalendarLegend(modifier = Modifier.align(Alignment.End))
        }
    }
}

@Composable
private fun TodayCalendarStrip(
    days: List<ReadingCalendarDay>,
    today: LocalDate,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val clickLabel = stringResource(CoreUiR.string.reading_calendar_open_day_records)
    val parentViewConfiguration = LocalViewConfiguration.current
    val calendarViewConfiguration =
        remember(parentViewConfiguration) {
            object : ViewConfiguration by parentViewConfiguration {
                override val minimumTouchTargetSize = DpSize.Zero
            }
        }
    // 밀집된 날짜 사이의 간격과 비활성 셀로 터치 영역이 확장되지 않게 한다.
    CompositionLocalProvider(LocalViewConfiguration provides calendarViewConfiguration) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            days.forEach { day ->
                val isToday = day.date == today
                val isSelected = day.date == selectedDate
                val isSelectable = day.pages > 0
                val dateDescription =
                    if (day.pages == 0) {
                        stringResource(
                            CoreUiR.string.reading_calendar_cell_no_record_content_description,
                            day.date.year,
                            day.date.monthNumber,
                            day.date.dayOfMonth,
                        )
                    } else {
                        pluralStringResource(
                            CoreUiR.plurals.reading_calendar_cell_content_description,
                            day.pages,
                            day.date.year,
                            day.date.monthNumber,
                            day.date.dayOfMonth,
                            day.pages,
                        )
                    }
                val contentDesc =
                    if (isToday) {
                        stringResource(CoreUiR.string.reading_calendar_today_content_description, dateDescription)
                    } else {
                        dateDescription
                    }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .then(
                                    if (isSelectable) {
                                        Modifier.clickable(
                                            onClickLabel = clickLabel,
                                            onClick = { onDateSelected(day.date) },
                                        )
                                    } else {
                                        Modifier
                                    },
                                ).clearAndSetSemantics {
                                    this.contentDescription = contentDesc
                                    if (isSelectable || isSelected) {
                                        this.selected = isSelected
                                    }
                                    if (isSelectable) {
                                        this.onClick(label = clickLabel) {
                                            onDateSelected(day.date)
                                            true
                                        }
                                    }
                                },
                        contentAlignment = Alignment.Center,
                    ) {
                        ReadingCalendarCell(
                            intensity = day.intensity,
                            isToday = isToday,
                            isSelected = isSelected,
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                        )
                    }
                    Text(
                        text = stringResource(weekdayLabels[day.date.dayOfWeek.value - 1]),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            }
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

@ManiculePreview
@Composable
private fun StatsCalendarCardTodayPreview() {
    val today = LocalDate(2026, 9, 24)
    ManiculePreviewTheme {
        StatsCalendarCard(
            days = (1..7).map {
                ReadingCalendarDay.of(
                    LocalDate(2026, 9, 18).plus(DatePeriod(days = it - 1)),
                    if (it % 2 == 0) 20 else 0,
                )
            },
            today = today,
            selectedDate = null,
            onDateSelected = {},
            isTodayPeriod = true,
        )
    }
}
