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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.model.ContributionDay
import com.leeseungyun1020.manicule.core.ui.preview.ContributionPreviewParameterProvider

@Composable
fun ContributionGrid(
    days: List<ContributionDay>,
    modifier: Modifier = Modifier,
    onDayClick: (ContributionDay) -> Unit = {},
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

    LazyHorizontalGrid(
        rows = GridCells.Fixed(7),
        state = state,
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        items(count = totalItems) { index ->
            if (index < paddingCount) {
                ContributionCell(intensity = null)
            } else {
                val day = days[index - paddingCount]
                ContributionCell(
                    intensity = day.intensity,
                    onClick = { onDayClick(day) },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ContributionGridPreview() {
    val dummyDays = ContributionPreviewParameterProvider().values.first()
    MaterialTheme {
        ContributionGrid(
            days = dummyDays,
            modifier = Modifier.height(150.dp),
        )
    }
}
