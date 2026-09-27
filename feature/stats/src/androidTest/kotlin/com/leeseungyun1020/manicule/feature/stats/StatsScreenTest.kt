package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.domain.stats.ReadingDayBook
import com.leeseungyun1020.manicule.core.model.Book
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
                    onRetryTodayBooks = {},
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
                    state = StatsUiState(
                        period = period(empty = true),
                        todayBooks = TodayBooksState.Content(today, emptyList()),
                    ),
                    onPeriodSelected = {},
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onRetryTodayBooks = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_calendar_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_today_books_empty)).assertIsDisplayed()
        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.stats_books_value, 0, 0)).assertIsDisplayed()
    }

    @Test
    fun empty_period_in_four_weeks_shows_empty_period_message() {
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(
                        period = period(empty = true, selectedPeriod = StatsPeriod.FOUR_WEEKS),
                    ),
                    onPeriodSelected = {},
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onRetryTodayBooks = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_empty_period)).assertIsDisplayed()
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
                    onRetryTodayBooks = {},
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

        composeRule.onNodeWithText(context.getString(R.string.stats_period_custom)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_title)).assertIsDisplayed()
    }

    @Test
    fun custom_tab_opens_sheet_and_applying_shows_custom_range_calendar_and_summary() {
        var state by mutableStateOf(StatsUiState(period(selectedPeriod = StatsPeriod.TODAY)))
        var appliedStart: LocalDate? = null
        var appliedEnd: LocalDate? = null

        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = state,
                    onPeriodSelected = { state = state.copy(period = period(selectedPeriod = it)) },
                    onApplyCustomPeriod = { start, end ->
                        appliedStart = start
                        appliedEnd = end
                        val customRange = CustomPeriodRange(start, end)
                        state = state.copy(
                            period = PeriodState.Content(
                                today = today,
                                selectedPeriod = StatsPeriod.CUSTOM,
                                customRange = customRange,
                                days = (0..27).map { index ->
                                    val date = start.plus(DatePeriod(days = index))
                                    ReadingCalendarDay.of(date, 10)
                                },
                                summary = PeriodSummary(start, end, 5, 280, 2),
                            ),
                        )
                        true
                    },
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onRetryTodayBooks = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_period_custom)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_start_date)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_end_date)).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_apply)).performClick()

        val expectedStart = today.minus(DatePeriod(days = 27))
        assertEquals(expectedStart, appliedStart)
        assertEquals(today, appliedEnd)

        // 적용 후 시트가 닫히고 커스텀 기간 범위 라벨, 달력, 요약 표시
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_title)).assertDoesNotExist()
        val dateRangeText = context.getString(
            R.string.stats_date_range,
            expectedStart.year,
            expectedStart.monthNumber,
            expectedStart.dayOfMonth,
            today.year,
            today.monthNumber,
            today.dayOfMonth,
        )
        composeRule.onNodeWithText(dateRangeText).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_calendar_title)).assertIsDisplayed()
        composeRule.onNodeWithText(context.resources.getQuantityString(R.plurals.stats_pages_value, 280, 280)).assertIsDisplayed()
    }

    @Test
    fun custom_period_sheet_dismiss_via_close_button_keeps_previous_period() {
        var state by mutableStateOf(StatsUiState(period(selectedPeriod = StatsPeriod.TODAY)))
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = state,
                    onPeriodSelected = { state = state.copy(period = period(selectedPeriod = it)) },
                    onApplyCustomPeriod = { _, _ -> true },
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onRetryTodayBooks = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_period_custom)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_title)).assertIsDisplayed()

        composeRule.onNodeWithContentDescription(context.getString(R.string.stats_close)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_title)).assertDoesNotExist()

        // 닫은 후에도 기존 TODAY의 단일 날짜 라벨 유지
        val singleDateText = context.getString(
            R.string.stats_single_date,
            today.year,
            today.monthNumber,
            today.dayOfMonth,
        )
        composeRule.onNodeWithText(singleDateText).assertIsDisplayed()
    }

    @Test
    fun custom_period_sheet_invalid_range_disables_apply_button_and_shows_error() {
        val invalidRange = CustomPeriodRange(today, today.minus(DatePeriod(days = 5)))
        composeRule.setContent {
            ManiculeTheme {
                com.leeseungyun1020.manicule.feature.stats.components.CustomPeriodContent(
                    today = today,
                    initialRange = invalidRange,
                    onApply = { _, _ -> },
                    onDismiss = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_error_order)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_apply)).assertIsNotEnabled()
    }

    @Test
    fun reentering_custom_sheet_shows_previously_applied_custom_range() {
        val customStart = LocalDate(2026, 6, 12)
        val customEnd = LocalDate(2026, 7, 9)
        val content = PeriodState.Content(
            today = today,
            selectedPeriod = StatsPeriod.CUSTOM,
            customRange = CustomPeriodRange(customStart, customEnd),
            days = emptyList(),
            summary = PeriodSummary(customStart, customEnd, 0, 0, 0),
        )
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(content),
                    onPeriodSelected = {},
                    onApplyCustomPeriod = { _, _ -> true },
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onRetryTodayBooks = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.stats_period_custom)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.stats_custom_period_title)).assertIsDisplayed()

        val expectedStartText = context.getString(
            R.string.stats_single_date,
            customStart.year,
            customStart.monthNumber,
            customStart.dayOfMonth,
        )
        val expectedEndText = context.getString(
            R.string.stats_single_date,
            customEnd.year,
            customEnd.monthNumber,
            customEnd.dayOfMonth,
        )
        composeRule.onNodeWithText(expectedStartText).assertIsDisplayed()
        composeRule.onNodeWithText(expectedEndText).assertIsDisplayed()
    }

    @Test
    fun empty_custom_period_shows_empty_period_text_and_zero_summary() {
        val customStart = LocalDate(2026, 6, 12)
        val customEnd = LocalDate(2026, 7, 9)
        val emptyCustomContent = PeriodState.Content(
            today = today,
            selectedPeriod = StatsPeriod.CUSTOM,
            customRange = CustomPeriodRange(customStart, customEnd),
            days = emptyList(),
            summary = PeriodSummary(customStart, customEnd, 0, 0, 0),
        )
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(emptyCustomContent),
                    onPeriodSelected = {},
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onRetryTodayBooks = {},
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
                    onRetryTodayBooks = {},
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
                    onRetryTodayBooks = {},
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

    @Test
    fun today_tab_displays_books_with_cover_title_records_pages_and_click_navigates() {
        var selectedIsbn: String? = null
        val todayPeriodContent = PeriodState.Content(
            today = today,
            selectedPeriod = StatsPeriod.TODAY,
            days = listOf(ReadingCalendarDay.of(today, 54)),
            summary = PeriodSummary(today, today, 1, 54, 1),
        )
        val book = testBook("isbn-1", "달러구트 꿈 백화점")
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(
                        period = todayPeriodContent,
                        todayBooks = TodayBooksState.Content(
                            today,
                            listOf(ReadingDayBook("isbn-1", book, 2, 54)),
                        ),
                    ),
                    onPeriodSelected = {},
                    onDateSelected = {},
                    onDismissDay = {},
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onRetryTodayBooks = {},
                    onBookSelected = { selectedIsbn = it },
                    consumeRefreshError = { true },
                )
            }
        }

        composeRule.onNodeWithText(
            context.resources.getQuantityString(R.plurals.stats_today_books_title, 1, 1),
        ).assertIsDisplayed()
        composeRule.onNodeWithText("달러구트 꿈 백화점").assertIsDisplayed()
        composeRule.onNodeWithText(
            context.resources.getQuantityString(R.plurals.stats_session_count, 2, 2),
        ).assertIsDisplayed()
        composeRule.onAllNodesWithText(
            context.resources.getQuantityString(R.plurals.stats_pages_value, 54, 54),
        ).assertCountEquals(2)

        composeRule.onNodeWithText("달러구트 꿈 백화점").performClick()
        assertEquals("isbn-1", selectedIsbn)
    }

    @Test
    fun today_cell_click_scrolls_to_today_books_and_does_not_open_sheet() {
        var day by mutableStateOf<DayState>(DayState.Closed)
        val todayPeriodContent = PeriodState.Content(
            today = today,
            selectedPeriod = StatsPeriod.TODAY,
            days = (0..6).map { index ->
                val date = today.minus(DatePeriod(days = 6 - index))
                ReadingCalendarDay.of(date, if (date == today) 45 else 0)
            },
            summary = PeriodSummary(today, today, 1, 45, 1),
        )
        composeRule.setContent {
            ManiculeTheme {
                StatsScreen(
                    state = StatsUiState(
                        period = todayPeriodContent,
                        day = day,
                        todayBooks = TodayBooksState.Content(
                            today,
                            listOf(ReadingDayBook("isbn-today", testBook("isbn-today", "오늘의 책"), 2, 45)),
                        ),
                    ),
                    onPeriodSelected = {},
                    onDateSelected = { date ->
                        day = DayState.Content(date, listOf(ReadingDayBook("isbn-sheet", null, 1, 45)))
                    },
                    onDismissDay = { day = DayState.Closed },
                    onRetryPeriod = {},
                    onRetryDay = {},
                    onRetryTodayBooks = {},
                    onBookSelected = {},
                    consumeRefreshError = { true },
                )
            }
        }

        val todayDateDescription = context.resources.getQuantityString(
            CoreUiR.plurals.reading_calendar_cell_content_description,
            45,
            today.year,
            today.monthNumber,
            today.dayOfMonth,
            45,
        )
        val todayDescription = context.getString(
            CoreUiR.string.reading_calendar_today_content_description,
            todayDateDescription,
        )
        composeRule.onNodeWithContentDescription(todayDescription).performClick()
        assertEquals(DayState.Closed, day)
        composeRule.onNodeWithText(
            context.resources.getQuantityString(R.plurals.stats_today_books_title, 1, 1),
        ).assertIsDisplayed()
        composeRule.onNodeWithText("오늘의 책").assertIsDisplayed()
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

    private fun testBook(
        isbn: String = "isbn-1",
        title: String = "달러구트 꿈 백화점",
        author: String = "이미예",
        publisher: String = "팩토리나인",
        publishedDate: LocalDate? = LocalDate(2020, 7, 8),
        coverUrl: String? = null,
    ) = Book(
        isbn = isbn,
        title = title,
        author = author,
        publisher = publisher,
        publishedDate = publishedDate,
        coverUrl = coverUrl,
        totalPages = 300,
        price = 13800,
        category = "소설",
        tableOfContentsUrl = null,
        introductionUrl = null,
        summaryUrl = null,
    )
}
