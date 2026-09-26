package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.domain.stats.ReadingDayBook
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.leeseungyun1020.manicule.core.ui.R as CoreUiR

@RunWith(AndroidJUnit4::class)
class StatsScreenTest {
    @get:Rule val composeRule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val today = LocalDate(2026, 9, 24)
    private val readDate = today.minus(DatePeriod(days = 2))

    @Test
    fun recorded_date_opens_matching_sheet_and_close_dismisses_it() {
        var day by mutableStateOf<DayState>(DayState.Closed)
        var selectedIsbn: String? = null
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(period(), day),
                    onPeriodSelected = {},
                    onDateSelected = { date ->
                        day = DayState.Content(date, listOf(ReadingDayBook("isbn", null, 2, 20)))
                    },
                    onDismissDay = { day = DayState.Closed },
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onBookSelected = { selectedIsbn = it },
                    consumeRefreshError = { true },
                )
            }
        }

        val emptyDate = today.minus(DatePeriod(days = 1))
        val emptyDescription = context.getString(
            CoreUiR.string.reading_calendar_cell_no_record_content_description,
            emptyDate.year,
            emptyDate.monthNumber,
            emptyDate.dayOfMonth,
        )
        composeRule.onNodeWithContentDescription(emptyDescription).assertHasNoClickAction()

        val readDescription = context.resources.getQuantityString(
            CoreUiR.plurals.reading_calendar_cell_content_description,
            20,
            readDate.year,
            readDate.monthNumber,
            readDate.dayOfMonth,
            20,
        )
        composeRule.onNodeWithContentDescription(readDescription).performClick()
        composeRule.onNodeWithText(
            context.resources.getQuantityString(
                R.plurals.stats_day_title,
                1,
                readDate.year,
                readDate.monthNumber,
                readDate.dayOfMonth,
                1,
            ),
        ).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_book_missing)).assertIsDisplayed()
        composeRule.onAllNodesWithText(context.resources.getQuantityString(R.plurals.stats_pages_value, 20, 20)).assertCountEquals(2)
        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.stats_session_count, 2, 2)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_book_missing)).assertHasClickAction().performClick()
        assertEquals("isbn", selectedIsbn)
        composeRule.onNodeWithContentDescription(context.getString(R.string.stats_close)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.stats_book_missing)).assertDoesNotExist()
    }

    @Test
    fun empty_period_still_shows_calendar_and_zero_summary() {
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(period(empty = true)),
                    onPeriodSelected = {},
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_calendar_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_empty_period)).assertIsDisplayed()
        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.stats_books_value, 0, 0)).assertIsDisplayed()
    }

    @Test
    fun segmented_buttons_displayed_and_tab_switching_triggers_callback() {
        var selectedPeriod: StatsPeriod? = null
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(period(selectedPeriod = StatsPeriod.TODAY)),
                    onPeriodSelected = { selectedPeriod = it },
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_period_today)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_period_four_weeks)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_period_one_year)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_period_custom)).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.stats_period_four_weeks)).performClick()
        assertEquals(StatsPeriod.FOUR_WEEKS, selectedPeriod)

        composeRule.onNodeWithText(context.getString(R.string.stats_period_one_year)).performClick()
        assertEquals(StatsPeriod.ONE_YEAR, selectedPeriod)

        // 직접 선택(CUSTOM)은 날짜 선택기/바텀시트가 구현될 때까지 탭해도 콜백이 호출되지 않는다.
        composeRule.onNodeWithText(context.getString(R.string.stats_period_custom)).performClick()
        assertEquals(StatsPeriod.ONE_YEAR, selectedPeriod)
    }

    @Test
    fun today_period_renders_horizontal_strip_and_recorded_date_opens_sheet() {
        var day by mutableStateOf<DayState>(DayState.Closed)
        val todayRecordDate = today.minus(DatePeriod(days = 1))
        val todayPeriodContent = PeriodState.Content(
            today = today,
            selectedPeriod = StatsPeriod.TODAY,
            days = (0..6).map { index ->
                val date = today.minus(DatePeriod(days = 6 - index))
                ReadingCalendarDay.of(date, if (date == todayRecordDate) 35 else 0)
            },
            summary = PeriodSummary(today, today, 1, 35, 1),
        )
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(todayPeriodContent, day),
                    onPeriodSelected = {},
                    onDateSelected = { date ->
                        day = DayState.Content(date, listOf(ReadingDayBook("isbn-today", null, 1, 35)))
                    },
                    onDismissDay = { day = DayState.Closed },
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_monday)).assertIsDisplayed()

        val readDescription = context.resources.getQuantityString(
            CoreUiR.plurals.reading_calendar_cell_content_description,
            35,
            todayRecordDate.year,
            todayRecordDate.monthNumber,
            todayRecordDate.dayOfMonth,
            35,
        )
        composeRule.onNodeWithContentDescription(readDescription).performClick()
        composeRule.onNodeWithText(
            context.resources.getQuantityString(
                R.plurals.stats_day_title,
                1,
                todayRecordDate.year,
                todayRecordDate.monthNumber,
                todayRecordDate.dayOfMonth,
                1,
            ),
        ).assertIsDisplayed()
    }

    @Test
    fun date_label_displays_single_date_for_today_and_date_range_for_other_periods() {
        var currentPeriod by mutableStateOf(StatsPeriod.TODAY)
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(period(selectedPeriod = currentPeriod)),
                    onPeriodSelected = { currentPeriod = it },
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        val singleDateText = context.getString(
            R.string.stats_single_date,
            today.year,
            today.monthNumber,
            today.dayOfMonth,
        )
        composeRule.onNodeWithText(singleDateText).assertIsDisplayed()

        currentPeriod = StatsPeriod.FOUR_WEEKS

        val start = today.minus(DatePeriod(days = 27))
        val dateRangeText = context.getString(
            R.string.stats_date_range,
            start.year,
            start.monthNumber,
            start.dayOfMonth,
            today.year,
            today.monthNumber,
            today.dayOfMonth,
        )
        composeRule.onNodeWithText(dateRangeText).assertIsDisplayed()
    }

    private fun period(
        empty: Boolean = false,
        selectedPeriod: StatsPeriod = StatsPeriod.TODAY,
    ): PeriodState.Content {
        val start = today.minus(DatePeriod(days = 27))
        return PeriodState.Content(
            today = today,
            selectedPeriod = selectedPeriod,
            days = (0..27).map { index ->
                val date = start.plus(DatePeriod(days = index))
                ReadingCalendarDay.of(date, if (!empty && date == readDate) 20 else 0)
            },
            summary = PeriodSummary(start, today, if (empty) 0 else 1, if (empty) 0 else 20, if (empty) 0 else 1),
        )
    }
}
