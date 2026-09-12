package com.leeseungyun1020.manicule.core.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
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
    val itemGap: Dp,
    val isDateSelectable: (ReadingCalendarDay) -> Boolean,
    val onDateSelected: ((LocalDate) -> Unit)?,
)

private data class CalendarDimensions(
    val itemSize: Dp,
    val itemGap: Dp,
    val height: Dp,
    val latestDayBottom: Int,
)

/**
 * 날짜 오름차순으로 연속된 [days]를 월~일 순서의 주별 열로 표시한다.
 * 선택 모드는 48dp 셀과 8dp 간격을 사용한다. 높이가 부족하면 셀을 줄이지 않고
 * 세로로 스크롤하며, 일반적인 세로 스크롤 화면 안에서는 전체 7행 높이를 사용한다.
 */
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
    val rangeStart = days.firstOrNull()?.date
    val rangeEnd = days.lastOrNull()?.date
    val paddingCount = (rangeStart?.dayOfWeek?.value ?: DayOfWeek.MONDAY.value) - DayOfWeek.MONDAY.value
    val weekCount = (paddingCount + days.size + CALENDAR_ROW_COUNT - 1) / CALENDAR_ROW_COUNT
    val listState = rememberLazyListState()
    val verticalScrollState = rememberScrollState()
    val firstWeekKey = (rangeStart?.toEpochDays() ?: 0) - paddingCount
    val dimensions = calendarDimensions(onDateSelected != null, rangeEnd, contentPadding)
    val gridConfig =
        ReadingCalendarGridConfig(
            today = today,
            selectedDate = selectedDate,
            itemSize = dimensions.itemSize,
            itemGap = dimensions.itemGap,
            isDateSelectable = isDateSelectable,
            onDateSelected = onDateSelected,
        )

    LaunchedEffect(rangeStart, rangeEnd, dimensions.itemSize) {
        if (weekCount > 0) {
            listState.scrollToItem(weekCount - 1)
        }
    }
    LaunchedEffect(rangeStart, rangeEnd, dimensions.latestDayBottom, verticalScrollState.viewportSize) {
        verticalScrollState.scrollTo((dimensions.latestDayBottom - verticalScrollState.viewportSize).coerceAtLeast(0))
    }

    BoxWithConstraints(modifier = modifier.heightIn(max = dimensions.height)) {
        val scrollModifier = if (maxHeight < dimensions.height) Modifier.verticalScroll(verticalScrollState) else Modifier
        Box(modifier = scrollModifier) {
            LazyRow(
                state = listState,
                contentPadding = contentPadding,
                horizontalArrangement = Arrangement.spacedBy(dimensions.itemGap),
                verticalAlignment = Alignment.Top,
            ) {
                items(
                    count = weekCount,
                    key = { week -> firstWeekKey + week * CALENDAR_ROW_COUNT },
                    contentType = { "calendar-week" },
                ) { week ->
                    ReadingCalendarWeek(
                        days = days,
                        firstDayIndex = week * CALENDAR_ROW_COUNT - paddingCount,
                        config = gridConfig,
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadingCalendarWeek(
    days: List<ReadingCalendarDay>,
    firstDayIndex: Int,
    config: ReadingCalendarGridConfig,
) {
    Column(verticalArrangement = Arrangement.spacedBy(config.itemGap)) {
        repeat(CALENDAR_ROW_COUNT) { row ->
            val index = firstDayIndex + row
            val day = days.getOrNull(index)
            if (day == null) {
                Box(modifier = Modifier.size(config.itemSize), contentAlignment = Alignment.Center) {
                    if (index < 0) {
                        ReadingCalendarCell(
                            intensity = null,
                            modifier = Modifier.size(MaterialTheme.size.calendarCell),
                        )
                    }
                }
            } else {
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
    Box(
        modifier =
            Modifier
                .then(
                    if (onClick == null) {
                        Modifier
                    } else {
                        Modifier.clickable(
                            onClickLabel = clickLabel,
                            onClick = onClick,
                        )
                    },
                ).semantics {
                    this.contentDescription = contentDescription
                    if (onClick != null || isSelected) {
                        selected = isSelected
                    }
                }.size(itemSize),
        contentAlignment = Alignment.Center,
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
private fun calendarDimensions(
    isInteractive: Boolean,
    lastDate: LocalDate?,
    contentPadding: PaddingValues,
): CalendarDimensions {
    val itemSize = if (isInteractive) ManiculeSize.touchTargetMin else MaterialTheme.size.calendarCell
    val itemGap = if (isInteractive) MaterialTheme.spacing.sm else MaterialTheme.size.calendarCellGap
    return with(LocalDensity.current) {
        // Column과 LazyRow의 항목·간격·패딩별 픽셀 반올림을 맞춘다.
        val sizePx = itemSize.roundToPx()
        val gapPx = itemGap.roundToPx()
        val topPadding = contentPadding.calculateTopPadding().roundToPx()
        val height = sizePx * CALENDAR_ROW_COUNT + gapPx * CALENDAR_GAP_COUNT +
            topPadding + contentPadding.calculateBottomPadding().roundToPx()
        val lastRow = (lastDate?.dayOfWeek?.value ?: DayOfWeek.MONDAY.value) - DayOfWeek.MONDAY.value
        CalendarDimensions(
            itemSize = itemSize,
            itemGap = itemGap,
            height = height.toDp(),
            latestDayBottom = topPadding + lastRow * (sizePx + gapPx) + sizePx,
        )
    }
}

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewSingle() {
    ManiculePreviewTheme {
        ReadingCalendarGrid(
            days = ReadingCalendarPreviewParameterProvider().values.first().take(1),
            today = LocalDate(2026, 7, 9),
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
        )
    }
}

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewConstrained() {
    ManiculePreviewTheme {
        ReadingCalendarGrid(
            days = ReadingCalendarPreviewParameterProvider().values.first(),
            today = LocalDate(2026, 7, 9),
            onDateSelected = {},
            contentPadding = PaddingValues(MaterialTheme.spacing.sm),
            modifier = Modifier.height(ManiculeSize.touchTargetMin * 3).padding(MaterialTheme.spacing.sm),
        )
    }
}

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewEmpty() {
    ManiculePreviewTheme {
        ReadingCalendarGrid(days = emptyList(), today = LocalDate(2026, 7, 9))
    }
}
