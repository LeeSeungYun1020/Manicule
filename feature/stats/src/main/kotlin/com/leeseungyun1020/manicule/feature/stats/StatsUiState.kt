package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.runtime.Immutable
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartBucket
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartUnit
import com.leeseungyun1020.manicule.core.domain.stats.ReadingDayBook
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import kotlinx.datetime.LocalDate

@Immutable
data class StatsUiState(
    val period: PeriodState = PeriodState.Loading,
    val day: DayState = DayState.Closed,
    val todayBooks: TodayBooksState = TodayBooksState.Hidden,
    val chart: ChartState = ChartState.Hidden,
)

@Immutable
data class ChartKey(
    val period: StatsPeriod,
    val start: LocalDate,
    val end: LocalDate,
    val unit: ReadingChartUnit,
)

@Immutable
sealed interface ChartState {
    data object Hidden : ChartState

    data class Loading(
        val key: ChartKey,
    ) : ChartState

    data class Error(
        val key: ChartKey,
    ) : ChartState

    data class Content(
        val key: ChartKey,
        val buckets: List<ReadingChartBucket>,
        val refreshErrorId: Int = 0,
    ) : ChartState
}

@Immutable
sealed interface PeriodState {
    data object Loading : PeriodState

    data object Error : PeriodState

    data class Content(
        val today: LocalDate,
        val days: List<ReadingCalendarDay>,
        val summary: PeriodSummary,
        val selectedPeriod: StatsPeriod = StatsPeriod.TODAY,
        val customRange: CustomPeriodRange? = null,
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
        val refreshErrorId: Int = 0,
    ) : DayState

    data class Error(
        val date: LocalDate,
    ) : DayState
}

@Immutable
sealed interface TodayBooksState {
    data object Hidden : TodayBooksState

    data object Loading : TodayBooksState

    data class Content(
        val date: LocalDate,
        val rows: List<ReadingDayBook>,
        val refreshErrorId: Int = 0,
    ) : TodayBooksState

    data object Error : TodayBooksState
}
