package com.leeseungyun1020.manicule.feature.stats

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.domain.stats.GetPeriodSummaryUseCase
import com.leeseungyun1020.manicule.core.domain.stats.GetReadingCalendarUseCase
import com.leeseungyun1020.manicule.core.domain.stats.GetReadingDayBooksUseCase
import com.leeseungyun1020.manicule.core.domain.time.observeToday
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import javax.inject.Inject

private const val SELECTED_DATE_KEY = "selected_date"
private const val SELECTED_PERIOD_KEY = "selected_period"

@HiltViewModel
class StatsViewModel
    @Inject
    constructor(
        private val getCalendar: GetReadingCalendarUseCase,
        private val getSummary: GetPeriodSummaryUseCase,
        private val getDayBooks: GetReadingDayBooksUseCase,
        private val clock: Clock,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val periodRetries = MutableStateFlow(0)
        private val dayRetries = MutableStateFlow(0)
        private val selectedPeriod = savedStateHandle.getStateFlow(SELECTED_PERIOD_KEY, StatsPeriod.TODAY)
        private val selectedDate = savedStateHandle.getStateFlow<String?>(SELECTED_DATE_KEY, null)
        private val consumedRefreshErrorIds = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
        private var nextRefreshErrorId = 0

        init {
            val restored = selectedDate.value?.let { parseDate(it) }
            if (selectedDate.value != null && (restored == null || !inCalendarRange(restored, clock.today(), selectedPeriod.value))) {
                savedStateHandle[SELECTED_DATE_KEY] = null
            }
        }

        private val periodState =
            combine(clock.observeToday(), selectedPeriod, periodRetries) { today, period, _ -> today to period }
                .flatMapLatest { (today, period) ->
                    val (calStart, calEnd) = calendarRange(today, period)
                    val (sumStart, sumEnd) = summaryRange(today, period)
                    combine(getCalendar(calStart, calEnd), getSummary(sumStart, sumEnd)) { days, summary ->
                        PeriodEvent.Ready(today, period, days, summary) as PeriodEvent
                    }.onStart { emit(PeriodEvent.Started(today, period)) }
                        .catch { emit(PeriodEvent.Failed(today, period)) }
                }.onEach { event ->
                    val today = event.today
                    val period = event.period
                    val selected = selectedDate.value?.let { parseDate(it) }
                    if (selected != null && !inCalendarRange(selected, today, period)) dismissDay()
                }.scan<PeriodEvent, PeriodState>(PeriodState.Loading) { previous, event ->
                    when (event) {
                        is PeriodEvent.Started ->
                            previous.takeIf {
                                it is PeriodState.Content && it.today == event.today && it.selectedPeriod == event.period
                            } ?: PeriodState.Loading
                        is PeriodEvent.Ready ->
                            PeriodState.Content(
                                today = event.today,
                                days = event.days,
                                summary = event.summary,
                                selectedPeriod = event.period,
                            )
                        is PeriodEvent.Failed -> {
                            val prior = previous as? PeriodState.Content
                            if (prior?.today == event.today && prior.selectedPeriod == event.period) {
                                prior.copy(refreshErrorId = ++nextRefreshErrorId)
                            } else {
                                PeriodState.Error
                            }
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodState.Loading)

        private val dayState =
            combine(selectedDate, dayRetries) { raw, _ -> raw?.let { parseDate(it) } }
                .flatMapLatest { date ->
                    if (date == null) {
                        flowOf<DayEvent>(DayEvent.Closed)
                    } else {
                        getDayBooks(date).map { DayEvent.Ready(date, it) as DayEvent }
                            .onStart { emit(DayEvent.Started(date)) }
                            .catch { emit(DayEvent.Failed(date)) }
                    }
                }.scan<DayEvent, DayState>(DayState.Closed) { previous, event ->
                    when (event) {
                        DayEvent.Closed -> DayState.Closed
                        is DayEvent.Started ->
                            previous.takeIf { it is DayState.Content && it.date == event.date }
                                ?: DayState.Loading(event.date)
                        is DayEvent.Ready -> DayState.Content(event.date, event.rows)
                        is DayEvent.Failed -> {
                            val prior = previous as? DayState.Content
                            if (prior?.date == event.date) {
                                prior.copy(refreshErrorId = ++nextRefreshErrorId)
                            } else {
                                DayState.Error(event.date)
                            }
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DayState.Closed)

        val uiState =
            combine(periodState, dayState) { period, day ->
                val visibleDay = when (period) {
                    is PeriodState.Content -> when (day) {
                        DayState.Closed -> day
                        is DayState.Loading -> day.takeIf { inCalendarRange(it.date, period.today, period.selectedPeriod) }
                            ?: DayState.Closed
                        is DayState.Content -> day.takeIf { inCalendarRange(it.date, period.today, period.selectedPeriod) }
                            ?: DayState.Closed
                        is DayState.Error -> day.takeIf { inCalendarRange(it.date, period.today, period.selectedPeriod) } ?: DayState.Closed
                    }
                    else -> DayState.Closed
                }
                StatsUiState(period, visibleDay)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

        fun selectPeriod(period: StatsPeriod) {
            if (selectedPeriod.value == period) return
            savedStateHandle[SELECTED_PERIOD_KEY] = period
            val currentToday = (periodState.value as? PeriodState.Content)?.today ?: clock.today()
            val selected = selectedDate.value?.let { parseDate(it) }
            if (selected != null && !inCalendarRange(selected, currentToday, period)) {
                dismissDay()
            }
        }

        fun selectDate(date: LocalDate) {
            val period = uiState.value.period as? PeriodState.Content ?: return
            if (period.days.none { it.date == date && it.pages > 0 }) return
            savedStateHandle[SELECTED_DATE_KEY] = date.toString()
        }

        fun dismissDay() {
            savedStateHandle[SELECTED_DATE_KEY] = null
        }

        fun retryPeriod() {
            periodRetries.value++
        }

        fun retryDay() {
            dayRetries.value++
        }

        fun consumeRefreshError(id: Int): Boolean {
            if (id <= 0) return false
            return consumedRefreshErrorIds.add(id)
        }

        private fun calendarRange(
            today: LocalDate,
            period: StatsPeriod,
        ): Pair<LocalDate, LocalDate> {
            val start =
                when (period) {
                    StatsPeriod.TODAY -> today.minus(DatePeriod(days = 6))
                    StatsPeriod.FOUR_WEEKS -> today.minus(DatePeriod(days = 27))
                    StatsPeriod.ONE_YEAR -> today.minus(DatePeriod(days = 364))
                    StatsPeriod.CUSTOM -> today.minus(DatePeriod(days = 27))
                }
            return start to today
        }

        private fun summaryRange(
            today: LocalDate,
            period: StatsPeriod,
        ): Pair<LocalDate, LocalDate> {
            val start =
                when (period) {
                    StatsPeriod.TODAY -> today
                    StatsPeriod.FOUR_WEEKS -> today.minus(DatePeriod(days = 27))
                    StatsPeriod.ONE_YEAR -> today.minus(DatePeriod(days = 364))
                    StatsPeriod.CUSTOM -> today.minus(DatePeriod(days = 27))
                }
            return start to today
        }

        private fun inCalendarRange(
            date: LocalDate,
            today: LocalDate,
            period: StatsPeriod,
        ): Boolean {
            val (start, end) = calendarRange(today, period)
            return date in start..end
        }

        private fun parseDate(raw: String): LocalDate? = runCatching { LocalDate.parse(raw) }.getOrNull()

        private sealed interface PeriodEvent {
            val today: LocalDate
            val period: StatsPeriod

            data class Started(
                override val today: LocalDate,
                override val period: StatsPeriod,
            ) : PeriodEvent

            data class Ready(
                override val today: LocalDate,
                override val period: StatsPeriod,
                val days: List<ReadingCalendarDay>,
                val summary: PeriodSummary,
            ) : PeriodEvent

            data class Failed(
                override val today: LocalDate,
                override val period: StatsPeriod,
            ) : PeriodEvent
        }

        private sealed interface DayEvent {
            data object Closed : DayEvent

            data class Started(
                val date: LocalDate,
            ) : DayEvent

            data class Ready(
                val date: LocalDate,
                val rows: List<com.leeseungyun1020.manicule.core.domain.stats.ReadingDayBook>,
            ) : DayEvent

            data class Failed(
                val date: LocalDate,
            ) : DayEvent
        }
    }
