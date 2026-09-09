package com.leeseungyun1020.manicule.core.ui.calendar

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.ui.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val TEST_CALENDAR_ROW_COUNT = 7
private const val TEST_CALENDAR_GAP_COUNT = TEST_CALENDAR_ROW_COUNT - 1

@RunWith(AndroidJUnit4::class)
class ReadingCalendarGridTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun gridPlacesMondayThroughSundayInOneColumn() {
        val monday = LocalDate(2026, 7, 6)
        val days = (0..TEST_CALENDAR_ROW_COUNT).map { offset ->
            ReadingCalendarDay.of(monday.plus(DatePeriod(days = offset)), pages = offset + 1)
        }

        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2026, 7, 20),
                    modifier =
                        Modifier
                            .width(ManiculeSize.calendarCell)
                            .height(compactGridHeight),
                )
            }
        }

        val bounds = days.map { day ->
            composeTestRule
                .onNodeWithContentDescription(descriptionFor(day))
                .getUnclippedBoundsInRoot()
        }
        bounds.drop(1).take(TEST_CALENDAR_GAP_COUNT).forEach { boundsForDay ->
            assertEquals(bounds.first().left, boundsForDay.left)
        }
        bounds.take(TEST_CALENDAR_ROW_COUNT).zipWithNext().forEach { (previous, next) ->
            assertTrue(previous.top < next.top)
        }
        assertTrue(bounds.last().left > bounds.first().left)
    }

    @Test
    fun recordedDayIsClickableAndReportsSelection() {
        val day = ReadingCalendarDay.of(LocalDate(2026, 7, 6), pages = 32)
        var selectedDate: LocalDate? = null

        composeTestRule.setContent {
            ManiculeTheme {
                var selection by remember { mutableStateOf<LocalDate?>(null) }
                ReadingCalendarGrid(
                    days = listOf(day),
                    today = LocalDate(2026, 7, 20),
                    selectedDate = selection,
                    isDateSelectable = { it.pages > 0 },
                    onDateSelected = {
                        selectedDate = it
                        selection = it
                    },
                    modifier =
                        Modifier
                            .width(ManiculeSize.touchTargetMin)
                            .height(interactiveGridHeight),
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(descriptionFor(day))
            .assertHasClickAction()
            .assertWidthIsAtLeast(ManiculeSize.touchTargetMin)
            .assertHeightIsAtLeast(ManiculeSize.touchTargetMin)
            .performClick()
            .assertIsSelected()
        composeTestRule.runOnIdle {
            assertEquals(day.date, selectedDate)
        }
    }

    @Test
    fun emptyDayDoesNotExposeClickAction() {
        val day = ReadingCalendarDay.of(LocalDate(2026, 7, 6), pages = 0)

        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = listOf(day),
                    today = LocalDate(2026, 7, 20),
                    isDateSelectable = { it.pages > 0 },
                    onDateSelected = {},
                    modifier =
                        Modifier
                            .width(ManiculeSize.touchTargetMin)
                            .height(interactiveGridHeight),
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(descriptionFor(day))
            .assertHasNoClickAction()
    }

    @Test
    fun verticalContentPaddingDoesNotShrinkOrOverlapSelectableDays() {
        val monday = ReadingCalendarDay.of(LocalDate(2026, 7, 6), pages = 1)
        val tuesday = ReadingCalendarDay.of(LocalDate(2026, 7, 7), pages = 1)
        val days = listOf(monday, tuesday)

        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2026, 7, 20),
                    onDateSelected = {},
                    contentPadding = PaddingValues(vertical = ManiculeSpacing.lg),
                    modifier =
                        Modifier
                            .width(ManiculeSize.touchTargetMin)
                            .height(interactiveGridHeight),
                )
            }
        }

        days.forEach { day ->
            composeTestRule
                .onNodeWithContentDescription(descriptionFor(day))
                .assertHasClickAction()
                .assertWidthIsAtLeast(ManiculeSize.touchTargetMin)
                .assertHeightIsAtLeast(ManiculeSize.touchTargetMin)
        }
        val mondayBounds =
            composeTestRule
                .onNodeWithContentDescription(descriptionFor(monday))
                .getUnclippedBoundsInRoot()
        val tuesdayBounds =
            composeTestRule
                .onNodeWithContentDescription(descriptionFor(tuesday))
                .getUnclippedBoundsInRoot()
        assertTrue(mondayBounds.bottom <= tuesdayBounds.top)
    }

    @Test
    fun todayIsIncludedInContentDescription() {
        val today = ReadingCalendarDay.of(LocalDate(2026, 7, 6), pages = 12)
        val baseDescription = descriptionFor(today)
        val todayDescription = context.getString(R.string.reading_calendar_today_content_description, baseDescription)

        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = listOf(today),
                    today = today.date,
                    modifier =
                        Modifier
                            .width(ManiculeSize.calendarCell)
                            .height(compactGridHeight),
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(todayDescription)
            .assertIsDisplayed()
    }

    @Test
    fun latestDayIsInitiallyDisplayed() {
        val start = LocalDate(2026, 4, 27)
        val days = (0 until 70).map { offset ->
            ReadingCalendarDay.of(start.plus(DatePeriod(days = offset)), pages = 1)
        }

        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = days.last().date,
                    modifier =
                        Modifier
                            .width(ManiculeSize.calendarCell * 2)
                            .height(compactGridHeight),
                )
            }
        }

        val lastDescription =
            context.getString(
                R.string.reading_calendar_today_content_description,
                descriptionFor(days.last()),
            )
        composeTestRule
            .onNodeWithContentDescription(lastDescription)
            .assertIsDisplayed()
    }

    private fun descriptionFor(day: ReadingCalendarDay): String =
        if (day.pages == 0) {
            context.getString(
                R.string.reading_calendar_cell_no_record_content_description,
                day.date.year,
                day.date.monthNumber,
                day.date.dayOfMonth,
            )
        } else {
            context.resources.getQuantityString(
                R.plurals.reading_calendar_cell_content_description,
                day.pages,
                day.date.year,
                day.date.monthNumber,
                day.date.dayOfMonth,
                day.pages,
            )
        }

    private val compactGridHeight =
        ManiculeSize.calendarCell * TEST_CALENDAR_ROW_COUNT +
            ManiculeSize.calendarCellGap * TEST_CALENDAR_GAP_COUNT

    private val interactiveGridHeight =
        ManiculeSize.touchTargetMin * TEST_CALENDAR_ROW_COUNT +
            ManiculeSpacing.sm * TEST_CALENDAR_GAP_COUNT
}
