package com.leeseungyun1020.manicule.core.ui.calendar

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.model.ContributionDay
import com.leeseungyun1020.manicule.core.ui.R
import com.leeseungyun1020.manicule.core.ui.preview.ReadingCalendarPreviewParameterProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private data class ReadingCalendarSelectionEvent(
    val date: LocalDate,
    val id: Int,
)

@Composable
fun ReadingCalendarGrid(
    days: List<ContributionDay>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    // 첫 번째 데이터의 요일을 확인하여 월요일(1) 기준 시작 위치 조정 패딩 생성
    val paddingCount =
        days
            .firstOrNull()
            ?.date
            ?.dayOfWeek
            ?.value
            ?.minus(1) ?: 0
    val totalItems = paddingCount + days.size

    // 가장 최근 데이터가 있는 우측 끝단으로 초기 스크롤 설정
    val initialIndex = maxOf(0, totalItems - 1)
    val gridState = rememberLazyGridState(initialFirstVisibleItemIndex = initialIndex)

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    var selectionEvent by remember { mutableStateOf<ReadingCalendarSelectionEvent?>(null) }

    fun selectDay(day: ContributionDay) {
        selectionEvent =
            ReadingCalendarSelectionEvent(
                date = day.date,
                id = (selectionEvent?.id ?: 0) + 1,
            )
    }

    LazyHorizontalGrid(
        rows = GridCells.Fixed(7),
        state = gridState,
        modifier =
            modifier.pointerInput(days, paddingCount) {
                detectTapGestures { position ->
                    gridState.layoutInfo.visibleItemsInfo
                        .firstOrNull { item -> item.contains(position) }
                        ?.index
                        ?.minus(paddingCount)
                        ?.let(days::getOrNull)
                        ?.let(::selectDay)
                }
            },
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.size.calendarCellGap),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.size.calendarCellGap),
    ) {
        items(count = totalItems) { index ->
            if (index < paddingCount) {
                ReadingCalendarCell(intensity = null)
            } else {
                val day = days[index - paddingCount]
                val contentDescription =
                    pluralStringResource(
                        id = R.plurals.reading_calendar_cell_content_description,
                        count = day.pages,
                        day.date.year,
                        day.date.monthNumber,
                        day.date.dayOfMonth,
                        day.pages,
                    )
                ReadingCalendarDayItem(
                    day = day,
                    isToday = day.date == today,
                    selectionEvent = selectionEvent,
                    contentDescription = contentDescription,
                )
            }
        }
    }
}

private fun LazyGridItemInfo.contains(position: Offset): Boolean =
    position.x >= offset.x &&
        position.x < offset.x + size.width &&
        position.y >= offset.y &&
        position.y < offset.y + size.height

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadingCalendarDayItem(
    day: ContributionDay,
    isToday: Boolean,
    selectionEvent: ReadingCalendarSelectionEvent?,
    contentDescription: String,
) {
    val tooltipState = rememberTooltipState()
    val selectedEvent = selectionEvent?.takeIf { it.date == day.date }
    val isTooltipVisible = selectedEvent != null && tooltipState.isVisible

    LaunchedEffect(selectedEvent) {
        if (selectedEvent != null) {
            tooltipState.show()
        }
    }

    TooltipBox(
        positionProvider =
            TooltipDefaults.rememberTooltipPositionProvider(
                positioning = TooltipAnchorPosition.Above,
            ),
        tooltip = {
            PlainTooltip {
                Text(text = contentDescription)
            }
        },
        state = tooltipState,
        enableUserInput = false,
    ) {
        ReadingCalendarCell(
            intensity = day.intensity,
            isToday = isToday,
            isSelected = isTooltipVisible,
            modifier =
                Modifier.semantics {
                    this.contentDescription = contentDescription
                    selected = isTooltipVisible
                },
        )
    }
}

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewSingle() {
    ManiculeTheme {
        ReadingCalendarGrid(
            days = ReadingCalendarPreviewParameterProvider().values.first().take(1),
            modifier = Modifier.height(100.dp),
        )
    }
}

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewSome() {
    ManiculeTheme {
        ReadingCalendarGrid(
            days = ReadingCalendarPreviewParameterProvider().values.first().take(5),
            modifier = Modifier.height(100.dp),
        )
    }
}

@ManiculePreview
@Composable
private fun ReadingCalendarGridPreviewMulti() {
    ManiculeTheme {
        ReadingCalendarGrid(
            days = ReadingCalendarPreviewParameterProvider().values.first(),
            modifier = Modifier.height(100.dp),
        )
    }
}
