package com.leeseungyun1020.manicule.core.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.ui.R
import com.leeseungyun1020.manicule.core.ui.preview.ReadingCalendarPreviewParameterProvider
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

private const val CALENDAR_ROW_COUNT = 7
private const val CALENDAR_GAP_COUNT = CALENDAR_ROW_COUNT - 1

private data class ReadingCalendarGridConfig(
    val today: LocalDate,
    val selectedDate: LocalDate?,
    val itemSize: Dp,
    val isDateSelectable: (ReadingCalendarDay) -> Boolean,
    val onDateSelected: ((LocalDate) -> Unit)?,
)

@Composable
fun ReadingCalendarGrid(
    days: List<ReadingCalendarDay>,
    today: LocalDate,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate? = null,
    isDateSelectable: (ReadingCalendarDay) -> Boolean = { true },
    onDateSelected: ((LocalDate) -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val paddingCount =
        days
            .firstOrNull()
            ?.date
            ?.dayOfWeek
            ?.let { it.value - DayOfWeek.MONDAY.value }
            ?: 0
    val totalItems = paddingCount + days.size
    val gridState = rememberLazyGridState()
    val rangeStart = days.firstOrNull()?.date
    val rangeEnd = days.lastOrNull()?.date
    val isInteractive = onDateSelected != null
    val itemSize = if (isInteractive) ManiculeSize.touchTargetMin else MaterialTheme.size.calendarCell
    val itemGap = if (isInteractive) MaterialTheme.spacing.sm else MaterialTheme.size.calendarCellGap
    val gridHeight =
        calendarGridHeight(
            itemSize = itemSize,
            itemGap = itemGap,
            contentPadding = contentPadding,
        )
    val gridConfig =
        ReadingCalendarGridConfig(
            today = today,
            selectedDate = selectedDate,
            itemSize = itemSize,
            isDateSelectable = isDateSelectable,
            onDateSelected = onDateSelected,
        )

    LaunchedEffect(rangeStart, rangeEnd, paddingCount) {
        if (totalItems > 0) {
            gridState.scrollToItem(totalItems - 1)
        }
    }

    Box(modifier = modifier) {
        LazyHorizontalGrid(
            rows = GridCells.Fixed(CALENDAR_ROW_COUNT),
            state = gridState,
            modifier = Modifier.height(gridHeight),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(itemGap),
            verticalArrangement = Arrangement.spacedBy(itemGap),
        ) {
            readingCalendarItems(days = days, paddingCount = paddingCount, config = gridConfig)
        }
    }
}

private fun LazyGridScope.readingCalendarItems(
    days: List<ReadingCalendarDay>,
    paddingCount: Int,
    config: ReadingCalendarGridConfig,
) {
    items(
        count = paddingCount + days.size,
        key = { index ->
            if (index < paddingCount) {
                "calendar-padding-$index"
            } else {
                "calendar-day-${days[index - paddingCount].date}"
            }
        },
        contentType = { index ->
            if (index < paddingCount) CalendarItemType.Padding else CalendarItemType.Day
        },
    ) { index ->
        if (index < paddingCount) {
            ReadingCalendarGridItem(itemSize = config.itemSize) {
                ReadingCalendarCell(
                    intensity = null,
                    modifier = Modifier.size(MaterialTheme.size.calendarCell),
                )
            }
        } else {
            val day = days[index - paddingCount]
            val onClick =
                config.onDateSelected
                    ?.takeIf { config.isDateSelectable(day) }
                    ?.let { callback -> { callback(day.date) } }
            ReadingCalendarDayItem(
                day = day,
                today = config.today,
                selectedDate = config.selectedDate,
                itemSize = config.itemSize,
                onClick = onClick,
            )
        }
    }
}

@Composable
private fun ReadingCalendarDayItem(
    day: ReadingCalendarDay,
    today: LocalDate,
    selectedDate: LocalDate?,
    itemSize: Dp,
    onClick: (() -> Unit)?,
) {
    val isToday = day.date == today
    val isSelected = day.date == selectedDate
    val contentDescription = readingCalendarContentDescription(day = day, isToday = isToday)
    val clickLabel = stringResource(R.string.reading_calendar_open_day_records)
    ReadingCalendarGridItem(
        itemSize = itemSize,
        modifier =
            Modifier
                .then(
                    if (onClick == null) {
                        Modifier
                    } else {
                        Modifier
                            .minimumInteractiveComponentSize()
                            .clickable(
                                onClickLabel = clickLabel,
                                onClick = onClick,
                            )
                    },
                ).semantics {
                    this.contentDescription = contentDescription
                    if (onClick != null || isSelected) {
                        selected = isSelected
                    }
                },
    ) {
        ReadingCalendarCell(
            intensity = day.intensity,
            isToday = isToday,
            isSelected = isSelected,
            modifier = Modifier.size(MaterialTheme.size.calendarCell),
        )
    }
}

@Composable
private fun readingCalendarContentDescription(
    day: ReadingCalendarDay,
    isToday: Boolean,
): String {
    val dateDescription =
        if (day.pages == 0) {
            stringResource(
                id = R.string.reading_calendar_cell_no_record_content_description,
                day.date.year,
                day.date.monthNumber,
                day.date.dayOfMonth,
            )
        } else {
            pluralStringResource(
                id = R.plurals.reading_calendar_cell_content_description,
                count = day.pages,
                day.date.year,
                day.date.monthNumber,
                day.date.dayOfMonth,
                day.pages,
            )
        }
    return if (isToday) {
        stringResource(R.string.reading_calendar_today_content_description, dateDescription)
    } else {
        dateDescription
    }
}

@Composable
private fun ReadingCalendarGridItem(
    itemSize: Dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .width(itemSize)
                .fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

private enum class CalendarItemType {
    Padding,
    Day,
}

private fun calendarGridHeight(
    itemSize: Dp,
    itemGap: Dp,
    contentPadding: PaddingValues = PaddingValues(),
): Dp =
    itemSize * CALENDAR_ROW_COUNT +
        itemGap * CALENDAR_GAP_COUNT +
        contentPadding.calculateTopPadding() +
        contentPadding.calculateBottomPadding()

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewSingle() {
    ManiculePreviewTheme {
        ReadingCalendarGrid(
            days = ReadingCalendarPreviewParameterProvider().values.first().take(1),
            today = LocalDate(2026, 7, 9),
            modifier = Modifier.height(calendarGridHeight(ManiculeSize.calendarCell, ManiculeSize.calendarCellGap)),
        )
    }
}

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewSome() {
    ManiculePreviewTheme {
        ReadingCalendarGrid(
            days = ReadingCalendarPreviewParameterProvider().values.first().take(5),
            today = LocalDate(2026, 7, 9),
            modifier = Modifier.height(calendarGridHeight(ManiculeSize.calendarCell, ManiculeSize.calendarCellGap)),
        )
    }
}

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewMulti() {
    ManiculePreviewTheme {
        ReadingCalendarGrid(
            days = ReadingCalendarPreviewParameterProvider().values.first(),
            today = LocalDate(2026, 7, 9),
            selectedDate = LocalDate(2026, 7, 8),
            isDateSelectable = { it.pages > 0 },
            onDateSelected = {},
            modifier = Modifier.height(calendarGridHeight(ManiculeSize.touchTargetMin, MaterialTheme.spacing.sm)),
        )
    }
}
