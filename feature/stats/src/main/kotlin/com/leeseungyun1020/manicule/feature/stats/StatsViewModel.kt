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
private const val CUSTOM_PERIOD_RANGE_KEY = "custom_period_range"

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
        private val todayBooksRetries = MutableStateFlow(0)
        private val selectedPeriod = savedStateHandle.getStateFlow(SELECTED_PERIOD_KEY, StatsPeriod.TODAY)
        private val customPeriodRange = savedStateHandle.getStateFlow<String?>(CUSTOM_PERIOD_RANGE_KEY, null)
        private val selectedDate = savedStateHandle.getStateFlow<String?>(SELECTED_DATE_KEY, null)
        private val consumedRefreshErrorIds = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
        private var nextRefreshErrorId = 0

        init {
            val today = clock.today()
            val rawCustomRange = savedStateHandle.get<String>(CUSTOM_PERIOD_RANGE_KEY)
            val parsedCustomRange = rawCustomRange?.let { CustomPeriodRange.parseIso(it) }
            val isCustomRangeValid = parsedCustomRange != null && parsedCustomRange.isValid(today)

            if (selectedPeriod.value == StatsPeriod.CUSTOM && !isCustomRangeValid) {
                savedStateHandle[SELECTED_PERIOD_KEY] = StatsPeriod.TODAY
            }
            if (rawCustomRange != null && !isCustomRangeValid) {
                savedStateHandle[CUSTOM_PERIOD_RANGE_KEY] = null
            }

            val currentPeriod = savedStateHandle.get<StatsPeriod>(SELECTED_PERIOD_KEY) ?: StatsPeriod.TODAY
            val activeCustom = if (currentPeriod == StatsPeriod.CUSTOM) parsedCustomRange else null
            val restored = selectedDate.value?.let { parseDate(it) }
            if (selectedDate.value != null &&
                (restored == null || shouldDismissSelectedDate(restored, today, currentPeriod, activeCustom))
            ) {
                savedStateHandle[SELECTED_DATE_KEY] = null
            }
        }

        private val periodState =
            combine(
                clock.observeToday(),
                selectedPeriod,
                customPeriodRange,
                periodRetries,
            ) { today, period, rawRange, _ ->
                val range = rawRange?.let { CustomPeriodRange.parseIso(it) }
                PeriodQuery(today, period, range)
            }.flatMapLatest { query ->
                val (today, period, customRange) = query
                val activeRange = if (period == StatsPeriod.CUSTOM) {
                    customRange?.takeIf { it.isValid(today) } ?: CustomPeriodRange.defaultFor(today)
                } else {
                    customRange
                }
                val (calStart, calEnd) = calendarRange(today, period, activeRange)
                val (sumStart, sumEnd) = summaryRange(today, period, activeRange)
                combine(getCalendar(calStart, calEnd), getSummary(sumStart, sumEnd)) { days, summary ->
                    PeriodEvent.Ready(today, period, activeRange, days, summary) as PeriodEvent
                }.onStart { emit(PeriodEvent.Started(today, period, activeRange)) }
                    .catch { emit(PeriodEvent.Failed(today, period, activeRange)) }
            }.onEach { event ->
                val today = event.today
                val period = event.period
                val activeRange = event.customRange
                val selected = selectedDate.value?.let { parseDate(it) }
                if (selected != null && shouldDismissSelectedDate(selected, today, period, activeRange)) {
                    dismissDay()
                }
            }.scan<PeriodEvent, PeriodState>(PeriodState.Loading) { previous, event ->
                when (event) {
                    is PeriodEvent.Started ->
                        previous.takeIf {
                            it is PeriodState.Content &&
                                it.today == event.today &&
                                it.selectedPeriod == event.period &&
                                it.customRange == event.customRange
                        } ?: PeriodState.Loading
                    is PeriodEvent.Ready ->
                        PeriodState.Content(
                            today = event.today,
                            days = event.days,
                            summary = event.summary,
                            selectedPeriod = event.period,
                            customRange = event.customRange,
                        )
                    is PeriodEvent.Failed -> {
                        val prior = previous as? PeriodState.Content
                        if (prior?.today == event.today &&
                            prior.selectedPeriod == event.period &&
                            prior.customRange == event.customRange
                        ) {
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

        private val todayBooksState =
            combine(clock.observeToday(), selectedPeriod, todayBooksRetries) { today, period, _ -> today to period }
                .flatMapLatest { (today, period) ->
                    if (period != StatsPeriod.TODAY) {
                        flowOf(TodayBooksEvent.Hidden)
                    } else {
                        getDayBooks(today).map { TodayBooksEvent.Ready(today, it) as TodayBooksEvent }
                            .onStart { emit(TodayBooksEvent.Started(today)) }
                            .catch { emit(TodayBooksEvent.Failed(today)) }
                    }
                }.scan<TodayBooksEvent, TodayBooksState>(TodayBooksState.Hidden) { previous, event ->
                    when (event) {
                        TodayBooksEvent.Hidden -> TodayBooksState.Hidden
                        is TodayBooksEvent.Started ->
                            previous.takeIf { it is TodayBooksState.Content && it.date == event.date }
                                ?: TodayBooksState.Loading
                        is TodayBooksEvent.Ready -> TodayBooksState.Content(event.date, event.rows)
                        is TodayBooksEvent.Failed -> {
                            val prior = previous as? TodayBooksState.Content
                            if (prior?.date == event.date) {
                                prior.copy(refreshErrorId = ++nextRefreshErrorId)
                            } else {
                                TodayBooksState.Error
                            }
                        }
                    }
                }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodayBooksState.Hidden)

        val uiState =
            combine(periodState, dayState, todayBooksState) { period, day, todayBooks ->
                val visibleDay = when (period) {
                    is PeriodState.Content -> when (day) {
                        DayState.Closed -> day
                        is DayState.Loading -> day.takeIf {
                            !shouldDismissSelectedDate(it.date, period.today, period.selectedPeriod, period.customRange)
                        } ?: DayState.Closed
                        is DayState.Content -> day.takeIf {
                            !shouldDismissSelectedDate(it.date, period.today, period.selectedPeriod, period.customRange)
                        } ?: DayState.Closed
                        is DayState.Error -> day.takeIf {
                            !shouldDismissSelectedDate(it.date, period.today, period.selectedPeriod, period.customRange)
                        } ?: DayState.Closed
                    }
                    else -> DayState.Closed
                }
                val visibleTodayBooks = when (period) {
                    is PeriodState.Content -> {
                        if (period.selectedPeriod == StatsPeriod.TODAY) {
                            todayBooks.takeIf {
                                when (it) {
                                    is TodayBooksState.Content -> it.date == period.today
                                    else -> true
                                }
                            } ?: TodayBooksState.Loading
                        } else {
                            TodayBooksState.Hidden
                        }
                    }
                    else -> TodayBooksState.Hidden
                }
                StatsUiState(period, visibleDay, visibleTodayBooks)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatsUiState())

        fun selectPeriod(period: StatsPeriod) {
            if (period == StatsPeriod.CUSTOM) {
                val today = (periodState.value as? PeriodState.Content)?.today ?: clock.today()
                val currentRange = customPeriodRange.value?.let { CustomPeriodRange.parseIso(it) }
                if (currentRange != null && currentRange.isValid(today)) {
                    savedStateHandle[SELECTED_PERIOD_KEY] = StatsPeriod.CUSTOM
                    checkDayInRange(today, StatsPeriod.CUSTOM, currentRange)
                }
                return
            }
            if (selectedPeriod.value == period) return
            savedStateHandle[SELECTED_PERIOD_KEY] = period
            val currentToday = (periodState.value as? PeriodState.Content)?.today ?: clock.today()
            checkDayInRange(currentToday, period, null)
        }

        fun applyCustomPeriod(
            start: LocalDate,
            end: LocalDate,
        ): Boolean {
            val today = (periodState.value as? PeriodState.Content)?.today ?: clock.today()
            val range = CustomPeriodRange(start, end)
            if (!range.isValid(today)) return false
            savedStateHandle[CUSTOM_PERIOD_RANGE_KEY] = range.formatIso()
            savedStateHandle[SELECTED_PERIOD_KEY] = StatsPeriod.CUSTOM
            checkDayInRange(today, StatsPeriod.CUSTOM, range)
            return true
        }

        private fun checkDayInRange(
            today: LocalDate,
            period: StatsPeriod,
            customRange: CustomPeriodRange?,
        ) {
            val selected = selectedDate.value?.let { parseDate(it) }
            if (selected != null && shouldDismissSelectedDate(selected, today, period, customRange)) {
                dismissDay()
            }
        }

        fun selectDate(date: LocalDate) {
            val period = uiState.value.period as? PeriodState.Content ?: return
            if (period.selectedPeriod == StatsPeriod.TODAY && date == period.today) return
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

        fun retryTodayBooks() {
            todayBooksRetries.value++
        }

        fun consumeRefreshError(id: Int): Boolean {
            if (id <= 0) return false
            return consumedRefreshErrorIds.add(id)
        }

        private fun calendarRange(
            today: LocalDate,
            period: StatsPeriod,
            customRange: CustomPeriodRange?,
        ): Pair<LocalDate, LocalDate> {
            val start =
                when (period) {
                    StatsPeriod.TODAY -> today.minus(DatePeriod(days = 6))
                    StatsPeriod.FOUR_WEEKS -> today.minus(DatePeriod(days = 27))
                    StatsPeriod.ONE_YEAR -> today.minus(DatePeriod(days = 363))
                    StatsPeriod.CUSTOM -> customRange?.start ?: today.minus(DatePeriod(days = 27))
                }
            val end =
                when (period) {
                    StatsPeriod.CUSTOM -> customRange?.end ?: today
                    else -> today
                }
            return start to end
        }

        private fun summaryRange(
            today: LocalDate,
            period: StatsPeriod,
            customRange: CustomPeriodRange?,
        ): Pair<LocalDate, LocalDate> {
            val start =
                when (period) {
                    StatsPeriod.TODAY -> today
                    StatsPeriod.FOUR_WEEKS -> today.minus(DatePeriod(days = 27))
                    StatsPeriod.ONE_YEAR -> today.minus(DatePeriod(days = 363))
                    StatsPeriod.CUSTOM -> customRange?.start ?: today.minus(DatePeriod(days = 27))
                }
            val end =
                when (period) {
                    StatsPeriod.CUSTOM -> customRange?.end ?: today
                    else -> today
                }
            return start to end
        }

        private fun inCalendarRange(
            date: LocalDate,
            today: LocalDate,
            period: StatsPeriod,
            customRange: CustomPeriodRange?,
        ): Boolean {
            val (start, end) = calendarRange(today, period, customRange)
            return date in start..end
        }

        private fun shouldDismissSelectedDate(
            date: LocalDate,
            today: LocalDate,
            period: StatsPeriod,
            customRange: CustomPeriodRange? = null,
        ): Boolean {
            if (!inCalendarRange(date, today, period, customRange)) return true
            return period == StatsPeriod.TODAY && date == today
        }

        private fun parseDate(raw: String): LocalDate? = runCatching { LocalDate.parse(raw) }.getOrNull()

        private data class PeriodQuery(
            val today: LocalDate,
            val period: StatsPeriod,
            val customRange: CustomPeriodRange?,
        )

        private sealed interface PeriodEvent {
            val today: LocalDate
            val period: StatsPeriod
            val customRange: CustomPeriodRange?

            data class Started(
                override val today: LocalDate,
                override val period: StatsPeriod,
                override val customRange: CustomPeriodRange?,
            ) : PeriodEvent

            data class Ready(
                override val today: LocalDate,
                override val period: StatsPeriod,
                override val customRange: CustomPeriodRange?,
                val days: List<ReadingCalendarDay>,
                val summary: PeriodSummary,
            ) : PeriodEvent

            data class Failed(
                override val today: LocalDate,
                override val period: StatsPeriod,
                override val customRange: CustomPeriodRange?,
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

        private sealed interface TodayBooksEvent {
            data object Hidden : TodayBooksEvent

            data class Started(
                val date: LocalDate,
            ) : TodayBooksEvent

            data class Ready(
                val date: LocalDate,
                val rows: List<com.leeseungyun1020.manicule.core.domain.stats.ReadingDayBook>,
            ) : TodayBooksEvent

            data class Failed(
                val date: LocalDate,
            ) : TodayBooksEvent
        }
    }
