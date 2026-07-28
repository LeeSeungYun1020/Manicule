package com.leeseungyun1020.manicule.core.ui.contribution

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.model.ContributionDay
import com.leeseungyun1020.manicule.core.ui.preview.ReadingCalendarPreviewParameterProvider
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun ReadingCalendarGrid(
    days: List<ContributionDay>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onDayClick: ((ContributionDay) -> Unit)? = null,
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
    val state = rememberLazyGridState(initialFirstVisibleItemIndex = initialIndex)

    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    LazyHorizontalGrid(
        rows = GridCells.Fixed(7),
        state = state,
        modifier = modifier,
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.size.calendarCellGap),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.size.calendarCellGap),
    ) {
        items(count = totalItems) { index ->
            if (index < paddingCount) {
                ReadingCalendarCell(intensity = null)
            } else {
                val day = days[index - paddingCount]
                ReadingCalendarCell(
                    intensity = day.intensity,
                    isToday = day.date == today,
                    onClick = onDayClick?.let { { it(day) } },
                )
            }
        }
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
