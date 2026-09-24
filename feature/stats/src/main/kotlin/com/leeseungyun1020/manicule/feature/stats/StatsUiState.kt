package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.domain.stats.ReadingDayBook
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import kotlinx.datetime.LocalDate

@Immutable
data class StatsUiState(
    val period: PeriodState = PeriodState.Loading,
    val day: DayState = DayState.Closed,
)

@Immutable
sealed interface PeriodState {
    data object Loading : PeriodState

    data object Error : PeriodState

    data class Content(
        val today: LocalDate,
        val days: List<ReadingCalendarDay>,
        val summary: PeriodSummary,
        val refreshErrorId: Int = 0,
    ) : PeriodState
}

@Immutable
sealed interface DayState {
    data object Closed : DayState

    data class Loading(
        val date: LocalDate,
    ) : DayState

    data class Content(
        val date: LocalDate,
        val rows: List<ReadingDayBook>,
        val refreshFailed: Boolean = false,
    ) : DayState

    data class Error(
        val date: LocalDate,
    ) : DayState
}
