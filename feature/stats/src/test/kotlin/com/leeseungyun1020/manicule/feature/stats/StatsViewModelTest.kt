package com.leeseungyun1020.manicule.feature.stats

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.BookSyncResult
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.domain.stats.GetPeriodSummaryUseCase
import com.leeseungyun1020.manicule.core.domain.stats.GetReadingCalendarUseCase
import com.leeseungyun1020.manicule.core.domain.stats.GetReadingDayBooksUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.DailyReading
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingTotals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class StatsViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val today = LocalDate(2024, 3, 1)
    private val repository = FakeRepository()
    private val fakeBooks = FakeBooks()
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2024-03-01T12:00:00Z")

        override fun timeZone(): TimeZone = TimeZone.UTC
    }

    @Test
    fun default_period_is_today_with_7_days_calendar_and_today_summary() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(
                record(1, LocalDate(2024, 2, 29), "a", 1, 10),
                record(2, today, "b", 1, 15),
            )
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val content = viewModel.uiState.value.period as PeriodState.Content
            assertThat(content.selectedPeriod).isEqualTo(StatsPeriod.TODAY)
            assertThat(content.days).hasSize(7)
            assertThat(content.days.first().date).isEqualTo(LocalDate(2024, 2, 24))
            assertThat(content.days.last().date).isEqualTo(today)
            assertThat(content.summary.pagesRead).isEqualTo(15)
            assertThat(content.summary.longestStreak).isEqualTo(1)
            job.cancel()
        }

    @Test
    fun switching_periods_updates_calendar_range_and_summary() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(
                record(1, LocalDate(2024, 2, 3), "a", 1, 10),
                record(2, today, "b", 1, 15),
            )
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.selectPeriod(StatsPeriod.FOUR_WEEKS)
            runCurrent()

            val fourWeeks = viewModel.uiState.value.period as PeriodState.Content
            assertThat(fourWeeks.selectedPeriod).isEqualTo(StatsPeriod.FOUR_WEEKS)
            assertThat(fourWeeks.days).hasSize(28)
            assertThat(fourWeeks.days.first().date).isEqualTo(LocalDate(2024, 2, 3))
            assertThat(fourWeeks.days.last().date).isEqualTo(today)
            assertThat(fourWeeks.summary.pagesRead).isEqualTo(25)

            viewModel.selectPeriod(StatsPeriod.ONE_YEAR)
            runCurrent()

            val oneYear = viewModel.uiState.value.period as PeriodState.Content
            assertThat(oneYear.selectedPeriod).isEqualTo(StatsPeriod.ONE_YEAR)
            assertThat(oneYear.days).hasSize(364)
            assertThat(oneYear.days.first().date).isEqualTo(LocalDate(2023, 3, 4))
            assertThat(oneYear.days.last().date).isEqualTo(today)
            job.cancel()
        }

    @Test
    fun switching_period_clears_out_of_range_selected_date() =
        runTest(dispatcherRule.dispatcher) {
            val dateInFourWeeksOnly = LocalDate(2024, 2, 10)
            repository.records.value = listOf(record(1, dateInFourWeeksOnly, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.selectPeriod(StatsPeriod.FOUR_WEEKS)
            runCurrent()

            viewModel.selectDate(dateInFourWeeksOnly)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isInstanceOf(DayState.Content::class.java)

            viewModel.selectPeriod(StatsPeriod.TODAY)
            runCurrent()

            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)
            job.cancel()
        }

    @Test
    fun period_restored_from_saved_state_handle() =
        runTest(dispatcherRule.dispatcher) {
            val handle = SavedStateHandle(mapOf("selected_period" to StatsPeriod.ONE_YEAR))
            val viewModel = viewModel(handle)
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val content = viewModel.uiState.value.period as PeriodState.Content
            assertThat(content.selectedPeriod).isEqualTo(StatsPeriod.ONE_YEAR)
            assertThat(content.days).hasSize(364)
            job.cancel()
        }

    @Test
    fun custom_period_default_range_is_recent_91_days_and_cancelling_maintains_previous_period() =
        runTest(dispatcherRule.dispatcher) {
            val defaultRange = CustomPeriodRange.defaultFor(today)
            assertThat(defaultRange.start).isEqualTo(LocalDate(2023, 12, 2))
            assertThat(defaultRange.end).isEqualTo(today)
            assertThat(defaultRange.dayCount).isEqualTo(91)

            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val initial = viewModel.uiState.value.period as PeriodState.Content
            assertThat(initial.selectedPeriod).isEqualTo(StatsPeriod.TODAY)
            // 취소 시 applyCustomPeriod가 호출되지 않아 기존 TODAY 기간이 유지된다
            runCurrent()
            val afterCancel = viewModel.uiState.value.period as PeriodState.Content
            assertThat(afterCancel.selectedPeriod).isEqualTo(StatsPeriod.TODAY)
            job.cancel()
        }

    @Test
    fun applying_custom_period_updates_calendar_range_and_summary() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(
                record(1, LocalDate(2024, 2, 10), "a", 1, 10),
                record(2, LocalDate(2024, 2, 20), "b", 1, 15),
            )
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val applied = viewModel.applyCustomPeriod(LocalDate(2024, 2, 10), LocalDate(2024, 2, 20))
            assertThat(applied).isTrue()
            runCurrent()

            val content = viewModel.uiState.value.period as PeriodState.Content
            assertThat(content.selectedPeriod).isEqualTo(StatsPeriod.CUSTOM)
            assertThat(content.customRange).isEqualTo(CustomPeriodRange(LocalDate(2024, 2, 10), LocalDate(2024, 2, 20)))
            assertThat(content.days).hasSize(11)
            assertThat(content.days.first().date).isEqualTo(LocalDate(2024, 2, 10))
            assertThat(content.days.last().date).isEqualTo(LocalDate(2024, 2, 20))
            assertThat(content.summary.pagesRead).isEqualTo(25)
            assertThat(content.summary.rangeStart).isEqualTo(LocalDate(2024, 2, 10))
            assertThat(content.summary.rangeEnd).isEqualTo(LocalDate(2024, 2, 20))
            job.cancel()
        }

    @Test
    fun reapplying_and_reentering_custom_period() =
        runTest(dispatcherRule.dispatcher) {
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.applyCustomPeriod(LocalDate(2024, 2, 10), LocalDate(2024, 2, 20))
            runCurrent()
            assertThat((viewModel.uiState.value.period as PeriodState.Content).days).hasSize(11)

            // 다른 기간으로 전환 시에도 기존 customRange는 유지됨
            viewModel.selectPeriod(StatsPeriod.FOUR_WEEKS)
            runCurrent()
            val fourWeeks = viewModel.uiState.value.period as PeriodState.Content
            assertThat(fourWeeks.selectedPeriod).isEqualTo(StatsPeriod.FOUR_WEEKS)
            assertThat(fourWeeks.customRange).isEqualTo(CustomPeriodRange(LocalDate(2024, 2, 10), LocalDate(2024, 2, 20)))

            // CUSTOM 재진입 시 기존 customRange로 복귀
            viewModel.selectPeriod(StatsPeriod.CUSTOM)
            runCurrent()
            val reentered = viewModel.uiState.value.period as PeriodState.Content
            assertThat(reentered.selectedPeriod).isEqualTo(StatsPeriod.CUSTOM)
            assertThat(reentered.days).hasSize(11)

            // 재적용 시 새로운 customRange로 변경
            viewModel.applyCustomPeriod(LocalDate(2024, 2, 1), LocalDate(2024, 2, 5))
            runCurrent()
            val reapplied = viewModel.uiState.value.period as PeriodState.Content
            assertThat(reapplied.days).hasSize(5)
            assertThat(reapplied.customRange).isEqualTo(CustomPeriodRange(LocalDate(2024, 2, 1), LocalDate(2024, 2, 5)))
            job.cancel()
        }

    @Test
    fun restored_custom_period_from_saved_state_handle() =
        runTest(dispatcherRule.dispatcher) {
            val handle = SavedStateHandle(
                mapOf(
                    "selected_period" to StatsPeriod.CUSTOM,
                    "custom_period_range" to "2024-02-01/2024-02-15",
                ),
            )
            val viewModel = viewModel(handle)
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val content = viewModel.uiState.value.period as PeriodState.Content
            assertThat(content.selectedPeriod).isEqualTo(StatsPeriod.CUSTOM)
            assertThat(content.customRange).isEqualTo(CustomPeriodRange(LocalDate(2024, 2, 1), LocalDate(2024, 2, 15)))
            assertThat(content.days).hasSize(15)
            job.cancel()
        }

    @Test
    fun invalid_or_missing_custom_range_on_restore_recovers_to_today() =
        runTest(dispatcherRule.dispatcher) {
            val invalidHandles = listOf(
                SavedStateHandle(mapOf("selected_period" to StatsPeriod.CUSTOM)),
                SavedStateHandle(mapOf("selected_period" to StatsPeriod.CUSTOM, "custom_period_range" to "invalid")),
                SavedStateHandle(mapOf("selected_period" to StatsPeriod.CUSTOM, "custom_period_range" to "2024-02-20/2024-02-10")),
                SavedStateHandle(mapOf("selected_period" to StatsPeriod.CUSTOM, "custom_period_range" to "2023-01-01/2024-02-01")),
                SavedStateHandle(mapOf("selected_period" to StatsPeriod.CUSTOM, "custom_period_range" to "2024-02-01/2024-03-05")),
            )

            for (handle in invalidHandles) {
                val vm = viewModel(handle)
                val job = backgroundScope.launch { vm.uiState.collect {} }
                runCurrent()

                val content = vm.uiState.value.period as PeriodState.Content
                assertThat(content.selectedPeriod).isEqualTo(StatsPeriod.TODAY)
                job.cancel()
            }
        }

    @Test
    fun custom_period_validation_boundaries_and_leap_year() =
        runTest(dispatcherRule.dispatcher) {
            // 1일 (start == end)
            val oneDay = CustomPeriodRange(LocalDate(2024, 2, 29), LocalDate(2024, 2, 29))
            assertThat(oneDay.validate(today)).isNull()
            assertThat(oneDay.dayCount).isEqualTo(1)

            // 윤년 포함 정확히 365일 (2023-03-03 ~ 2024-03-01: 2024-02-29 포함)
            val exact365 = CustomPeriodRange(LocalDate(2023, 3, 3), LocalDate(2024, 3, 1))
            assertThat(exact365.validate(today)).isNull()
            assertThat(exact365.dayCount).isEqualTo(365)

            // 윤년 포함 366일 (2023-03-02 ~ 2024-03-01) -> 거부
            val exact366 = CustomPeriodRange(LocalDate(2023, 3, 2), LocalDate(2024, 3, 1))
            assertThat(exact366.validate(today)).isEqualTo(CustomPeriodRange.ValidationError.EXCEEDS_MAX_DAYS)
            assertThat(exact366.dayCount).isEqualTo(366)

            // 역순 (start > end) -> 거부
            val reverse = CustomPeriodRange(LocalDate(2024, 2, 20), LocalDate(2024, 2, 10))
            assertThat(reverse.validate(today)).isEqualTo(CustomPeriodRange.ValidationError.START_AFTER_END)

            // 미래 날짜 (end > today) -> 거부
            val future = CustomPeriodRange(LocalDate(2024, 2, 20), LocalDate(2024, 3, 2))
            assertThat(future.validate(today)).isEqualTo(CustomPeriodRange.ValidationError.FUTURE_DATE)

            // 잘못된 범위 적용 시 ViewModel이 거부하고 상태를 변경하지 않음
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val applied = viewModel.applyCustomPeriod(LocalDate(2024, 2, 20), LocalDate(2024, 2, 10))
            assertThat(applied).isFalse()
            runCurrent()
            assertThat((viewModel.uiState.value.period as PeriodState.Content).selectedPeriod).isEqualTo(StatsPeriod.TODAY)
            job.cancel()
        }

    @Test
    fun custom_period_change_clears_out_of_range_selected_date() =
        runTest(dispatcherRule.dispatcher) {
            val dateInFirstRange = LocalDate(2024, 2, 5)
            repository.records.value = listOf(record(1, dateInFirstRange, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.applyCustomPeriod(LocalDate(2024, 2, 1), LocalDate(2024, 2, 10))
            runCurrent()

            viewModel.selectDate(dateInFirstRange)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isInstanceOf(DayState.Content::class.java)

            // 새 custom 범위 밖의 날짜이므로 시트가 닫힘
            viewModel.applyCustomPeriod(LocalDate(2024, 2, 15), LocalDate(2024, 2, 20))
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)
            job.cancel()
        }

    @Test
    fun custom_period_query_cancels_previous_flow_and_recovers_on_retry() =
        runTest(dispatcherRule.dispatcher) {
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            repository.failPeriod = true
            viewModel.applyCustomPeriod(LocalDate(2024, 2, 1), LocalDate(2024, 2, 10))
            runCurrent()
            assertThat(viewModel.uiState.value.period).isEqualTo(PeriodState.Error)

            repository.failPeriod = false
            viewModel.retryPeriod()
            runCurrent()
            val content = viewModel.uiState.value.period as PeriodState.Content
            assertThat(content.selectedPeriod).isEqualTo(StatsPeriod.CUSTOM)
            assertThat(content.days).hasSize(10)
            job.cancel()
        }

    @Test
    fun only_read_dates_open_and_removing_last_record_keeps_empty_sheet() =
        runTest(dispatcherRule.dispatcher) {
            val date = LocalDate(2024, 2, 29)
            repository.records.value = listOf(record(1, date, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.selectDate(today)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)

            viewModel.selectDate(date)
            runCurrent()
            val selected = viewModel.uiState.value.day as DayState.Content
            assertThat(selected.rows.single().pagesRead).isEqualTo(10)

            repository.records.value = emptyList()
            runCurrent()
            assertThat((viewModel.uiState.value.day as DayState.Content).rows).isEmpty()
            viewModel.dismissDay()
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)
            job.cancel()
        }

    @Test
    fun restored_out_of_range_date_is_cleared() =
        runTest(dispatcherRule.dispatcher) {
            val handle = SavedStateHandle(mapOf("selected_date" to "2024-01-01"))
            val viewModel = viewModel(handle)
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            assertThat(handle.get<String>("selected_date")).isNull()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)
            job.cancel()
        }

    @Test
    fun initial_period_error_recovers_on_retry() =
        runTest(dispatcherRule.dispatcher) {
            repository.failPeriod = true
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            assertThat(viewModel.uiState.value.period).isEqualTo(PeriodState.Error)

            repository.failPeriod = false
            viewModel.retryPeriod()
            runCurrent()
            assertThat(viewModel.uiState.value.period).isInstanceOf(PeriodState.Content::class.java)
            job.cancel()
        }

    @Test
    fun day_error_retries_without_losing_calendar() =
        runTest(dispatcherRule.dispatcher) {
            val date = LocalDate(2024, 2, 29)
            repository.records.value = listOf(record(1, date, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            repository.failDay = true
            viewModel.selectDate(date)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Error(date))
            assertThat(viewModel.uiState.value.period).isInstanceOf(PeriodState.Content::class.java)

            repository.failDay = false
            viewModel.retryDay()
            runCurrent()
            assertThat((viewModel.uiState.value.day as DayState.Content).rows.single().pagesRead).isEqualTo(10)
            job.cancel()
        }

    @Test
    fun day_refresh_failure_retains_prior_rows_with_refresh_error_id_and_recovers_on_retry() =
        runTest(dispatcherRule.dispatcher) {
            val date = LocalDate(2024, 2, 29)
            repository.records.value = listOf(record(1, date, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.selectDate(date)
            runCurrent()
            val contentBefore = viewModel.uiState.value.day as DayState.Content
            assertThat(contentBefore.rows).hasSize(1)
            assertThat(contentBefore.refreshErrorId).isEqualTo(0)

            repository.failDay = true
            repository.records.value = listOf(record(1, date, "a", 1, 20))
            runCurrent()

            val contentDuring = viewModel.uiState.value.day as DayState.Content
            assertThat(contentDuring.rows.single().pagesRead).isEqualTo(10)
            assertThat(contentDuring.refreshErrorId).isGreaterThan(0)
            assertThat(viewModel.consumeRefreshError(contentDuring.refreshErrorId)).isTrue()
            assertThat(viewModel.consumeRefreshError(contentDuring.refreshErrorId)).isFalse()

            repository.failDay = false
            viewModel.retryDay()
            runCurrent()

            val contentAfter = viewModel.uiState.value.day as DayState.Content
            assertThat(contentAfter.rows.single().pagesRead).isEqualTo(20)
            assertThat(contentAfter.refreshErrorId).isEqualTo(0)
            job.cancel()
        }

    @Test
    fun today_books_observed_in_today_period_and_hidden_in_other_periods() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(
                record(1, today, "a", 1, 10),
                record(2, today, "a", 11, 20),
                record(3, today, "b", 1, 15),
            )
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val initialBooks = viewModel.uiState.value.todayBooks as TodayBooksState.Content
            assertThat(initialBooks.date).isEqualTo(today)
            assertThat(initialBooks.rows).hasSize(2)
            assertThat(initialBooks.rows.first { it.isbn == "a" }.recordCount).isEqualTo(2)
            assertThat(initialBooks.rows.first { it.isbn == "a" }.pagesRead).isEqualTo(20)

            viewModel.selectPeriod(StatsPeriod.FOUR_WEEKS)
            runCurrent()
            assertThat(viewModel.uiState.value.todayBooks).isEqualTo(TodayBooksState.Hidden)

            viewModel.selectPeriod(StatsPeriod.TODAY)
            runCurrent()
            val restoredBooks = viewModel.uiState.value.todayBooks as TodayBooksState.Content
            assertThat(restoredBooks.date).isEqualTo(today)
            assertThat(restoredBooks.rows).hasSize(2)
            job.cancel()
        }

    @Test
    fun today_date_cannot_be_selected_in_today_period_but_can_in_four_weeks() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(record(1, today, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.selectDate(today)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)

            viewModel.selectPeriod(StatsPeriod.FOUR_WEEKS)
            runCurrent()

            viewModel.selectDate(today)
            runCurrent()
            val selected = viewModel.uiState.value.day as DayState.Content
            assertThat(selected.date).isEqualTo(today)
            assertThat(selected.rows.single().pagesRead).isEqualTo(10)
            job.cancel()
        }

    @Test
    fun today_selected_date_cleared_when_switching_to_today_period() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(record(1, today, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.selectPeriod(StatsPeriod.FOUR_WEEKS)
            runCurrent()

            viewModel.selectDate(today)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isInstanceOf(DayState.Content::class.java)

            viewModel.selectPeriod(StatsPeriod.TODAY)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)
            job.cancel()
        }

    @Test
    fun today_restored_selected_date_cleared_if_today_period() =
        runTest(dispatcherRule.dispatcher) {
            val handle = SavedStateHandle(
                mapOf(
                    "selected_period" to StatsPeriod.TODAY,
                    "selected_date" to today.toString(),
                ),
            )
            val viewModel = viewModel(handle)
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            assertThat(handle.get<String>("selected_date")).isNull()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)
            job.cancel()
        }

    @Test
    fun today_books_initial_error_recovers_on_retry_without_affecting_period_summary() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(record(1, today, "a", 1, 10))
            fakeBooks.failBook = true
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            assertThat(viewModel.uiState.value.period).isInstanceOf(PeriodState.Content::class.java)
            assertThat(viewModel.uiState.value.todayBooks).isEqualTo(TodayBooksState.Error)

            fakeBooks.failBook = false
            viewModel.retryTodayBooks()
            runCurrent()

            val content = viewModel.uiState.value.todayBooks as TodayBooksState.Content
            assertThat(content.rows.single().pagesRead).isEqualTo(10)
            job.cancel()
        }

    @Test
    fun today_books_refresh_failure_retains_prior_rows_with_refresh_error_id_and_recovers_on_retry() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(record(1, today, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val contentBefore = viewModel.uiState.value.todayBooks as TodayBooksState.Content
            assertThat(contentBefore.rows).hasSize(1)
            assertThat(contentBefore.refreshErrorId).isEqualTo(0)

            fakeBooks.failBook = true
            repository.records.value = listOf(record(1, today, "a", 1, 20))
            runCurrent()

            val contentDuring = viewModel.uiState.value.todayBooks as TodayBooksState.Content
            assertThat(contentDuring.rows.single().pagesRead).isEqualTo(10)
            assertThat(contentDuring.refreshErrorId).isGreaterThan(0)
            assertThat(viewModel.consumeRefreshError(contentDuring.refreshErrorId)).isTrue()
            assertThat(viewModel.consumeRefreshError(contentDuring.refreshErrorId)).isFalse()

            fakeBooks.failBook = false
            viewModel.retryTodayBooks()
            runCurrent()

            val contentAfter = viewModel.uiState.value.todayBooks as TodayBooksState.Content
            assertThat(contentAfter.rows.single().pagesRead).isEqualTo(20)
            assertThat(contentAfter.refreshErrorId).isEqualTo(0)
            job.cancel()
        }

    @Test
    fun clock_day_change_updates_today_books_and_does_not_leak_previous_day_rows() =
        runTest(dispatcherRule.dispatcher) {
            var currentInstant = Instant.parse("2024-03-01T23:59:00Z")
            val mutableClock = object : Clock {
                override fun now(): Instant = currentInstant

                override fun timeZone(): TimeZone = TimeZone.UTC
            }
            repository.records.value = listOf(
                record(1, today, "a", 1, 10),
                record(2, LocalDate(2024, 3, 2), "b", 1, 15),
            )
            val viewModel = viewModel(clock = mutableClock)
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val booksDay1 = viewModel.uiState.value.todayBooks as TodayBooksState.Content
            assertThat(booksDay1.date).isEqualTo(today)
            assertThat(booksDay1.rows.single().isbn).isEqualTo("a")

            currentInstant = Instant.parse("2024-03-02T00:01:00Z")
            testScheduler.advanceTimeBy(61_000)
            runCurrent()

            val booksDay2 = viewModel.uiState.value.todayBooks as TodayBooksState.Content
            assertThat(booksDay2.date).isEqualTo(LocalDate(2024, 3, 2))
            assertThat(booksDay2.rows.single().isbn).isEqualTo("b")
            job.cancel()
        }

    private fun viewModel(
        handle: SavedStateHandle = SavedStateHandle(),
        clock: Clock = this.clock,
    ) = StatsViewModel(
        GetReadingCalendarUseCase(repository),
        GetPeriodSummaryUseCase(repository),
        GetReadingDayBooksUseCase(repository, fakeBooks),
        clock,
        handle,
    )

    private fun record(
        id: Long,
        date: LocalDate,
        isbn: String,
        start: Int,
        end: Int,
    ) = ReadingRecord(id, isbn, date, LocalTime(12, 0), start, end)

    private class FakeRepository : StatsRepository {
        val records = MutableStateFlow<List<ReadingRecord>>(emptyList())
        var failPeriod = false
        var failDay = false

        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> =
            records.map { list ->
                if (start == end && failDay) error("day failed")
                if (start != end && failPeriod) error("period failed")
                list.filter { it.date in start..end }
            }

        override fun observeDailyReading(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<DailyReading>> =
            records.map { list ->
                if (failPeriod) error("calendar period failed")
                list.filter { it.date in start..end }.groupBy { it.date }
                    .map { (date, sessions) ->
                        DailyReading(date, sessions.sumOf { it.pagesRead }, sessions.distinctBy { it.isbn }.size)
                    }
            }

        override fun observeTotals(
            start: LocalDate,
            end: LocalDate,
        ): Flow<ReadingTotals> = flowOf(ReadingTotals(0, 0))

        override fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>> = flowOf(emptyList())
    }

    private class FakeBooks : BookRepository {
        var failBook = false

        override fun observeBook(isbn: String): Flow<Book?> =
            if (failBook) {
                kotlinx.coroutines.flow.flow { error("book failed") }
            } else {
                flowOf(null)
            }

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> = error("Unexpected sync")

        override fun searchBooks(query: String): Flow<PagingData<Book>> = flowOf(PagingData.empty())
    }
}
