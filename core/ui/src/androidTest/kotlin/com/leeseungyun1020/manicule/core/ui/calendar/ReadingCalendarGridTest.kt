package com.leeseungyun1020.manicule.core.ui.calendar

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeBorder
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
import kotlin.math.roundToInt

private const val TEST_CALENDAR_ROW_COUNT = 7
private const val TEST_CALENDAR_GAP_COUNT = TEST_CALENDAR_ROW_COUNT - 1
private const val TEST_CALENDAR_TAG = "reading-calendar"

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
                            .width(
                                ManiculeSize.calendarCell * 2 +
                                    ManiculeSize.calendarCellGap,
                            )
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
                            .testTag(TEST_CALENDAR_TAG),
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_CALENDAR_TAG)
            .assertHeightIsEqualTo(interactiveGridHeight + ManiculeSpacing.lg * 2)
        assertSelectableDaysDoNotOverlap(days)
    }

    @Test
    fun callerPaddingStaysOutsideSelectableDayTargets() {
        val monday = ReadingCalendarDay.of(LocalDate(2026, 7, 6), pages = 1)
        val tuesday = ReadingCalendarDay.of(LocalDate(2026, 7, 7), pages = 1)
        val days = listOf(monday, tuesday)

        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2026, 7, 20),
                    onDateSelected = {},
                    modifier =
                        Modifier
                            .width(ManiculeSize.touchTargetMin)
                            .testTag(TEST_CALENDAR_TAG)
                            .padding(vertical = ManiculeSpacing.lg),
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_CALENDAR_TAG)
            .assertHeightIsEqualTo(interactiveGridHeight + ManiculeSpacing.lg * 2)
        assertSelectableDaysDoNotOverlap(days)
    }

    @Test
    fun fixedHeightKeepsDayTargetsAndAllowsScrollingToSunday() {
        assertConstrainedCalendar(Modifier.height(interactiveGridHeight / 2))
    }

    @Test
    fun fixedHeightWithBothPaddingsKeepsDayTargets() {
        assertConstrainedCalendar(
            modifier = Modifier.height(interactiveGridHeight / 2).padding(vertical = ManiculeSpacing.lg),
            contentPadding = PaddingValues(ManiculeSpacing.lg),
        )
    }

    @Test
    fun paddingBeforeFixedHeightKeepsDayTargets() {
        assertConstrainedCalendar(
            modifier = Modifier.padding(vertical = ManiculeSpacing.lg).height(interactiveGridHeight / 2),
            contentPadding = PaddingValues(vertical = ManiculeSpacing.lg),
        )
    }

    @Test
    fun parentHeightLimitWithBothPaddingsKeepsDayTargets() {
        assertConstrainedCalendar(
            modifier = Modifier.padding(vertical = ManiculeSpacing.lg),
            contentPadding = PaddingValues(vertical = ManiculeSpacing.lg),
            parentHeight = interactiveGridHeight / 2,
        )
    }

    @Test
    fun calendarInsideScrollingScreenKeepsItsNaturalHeight() {
        val days = (0 until TEST_CALENDAR_ROW_COUNT).map { offset ->
            ReadingCalendarDay.of(LocalDate(2026, 7, 6).plus(DatePeriod(days = offset)), pages = 1)
        }
        composeTestRule.setContent {
            ManiculeTheme {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    ReadingCalendarGrid(
                        days = days,
                        today = LocalDate(2026, 7, 20),
                        onDateSelected = {},
                        modifier = Modifier.width(ManiculeSize.touchTargetMin).testTag(TEST_CALENDAR_TAG),
                    )
                }
            }
        }
        composeTestRule.onNodeWithTag(TEST_CALENDAR_TAG).assertHeightIsEqualTo(interactiveGridHeight)
        assertSelectableDaysDoNotOverlap(days)
    }

    private fun assertConstrainedCalendar(
        modifier: Modifier,
        contentPadding: PaddingValues = PaddingValues(),
        parentHeight: Dp = interactiveGridHeight,
    ) {
        val days = (0 until TEST_CALENDAR_ROW_COUNT).map { offset ->
            ReadingCalendarDay.of(LocalDate(2026, 7, 6).plus(DatePeriod(days = offset)), pages = 1)
        }
        val selections = mutableListOf<LocalDate>()
        composeTestRule.setContent {
            ManiculeTheme {
                Column {
                    Box(Modifier.heightIn(max = parentHeight)) {
                        ReadingCalendarGrid(
                            days = days,
                            today = LocalDate(2026, 7, 20),
                            onDateSelected = selections::add,
                            contentPadding = contentPadding,
                            modifier = Modifier.width(ManiculeSize.touchTargetMin * 3).then(modifier),
                        )
                    }
                    Box(Modifier.width(ManiculeSize.touchTargetMin).height(ManiculeSize.touchTargetMin).testTag("sibling"))
                }
            }
        }

        assertSelectableDaysDoNotOverlap(days)
        days.forEach { day ->
            val node = composeTestRule.onNodeWithContentDescription(descriptionFor(day))
            val viewport = composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
            val viewportBounds = viewport.getUnclippedBoundsInRoot()
            val targetBounds = node.getUnclippedBoundsInRoot()
            val scrollDelta =
                if (targetBounds.top < viewportBounds.top) {
                    targetBounds.top - viewportBounds.top
                } else {
                    (targetBounds.bottom - viewportBounds.bottom).coerceAtLeast(0.dp)
                }
            viewport.performSemanticsAction(SemanticsActions.ScrollBy) {
                it(0f, with(composeTestRule.density) { scrollDelta.toPx() })
            }
            node.assertIsDisplayed().performTouchInput {
                click(topLeft + Offset(1f, 1f))
                click(bottomRight - Offset(1f, 1f))
            }
            val bounds = node.getUnclippedBoundsInRoot()
            val siblingBounds = composeTestRule.onNodeWithTag("sibling").getUnclippedBoundsInRoot()
            assertTrue("The entire target must be reachable", bounds.top >= viewportBounds.top && bounds.bottom <= viewportBounds.bottom)
            assertTrue("Day must stay above the following content", bounds.bottom <= siblingBounds.top)
        }
        composeTestRule.runOnIdle {
            assertEquals(days.flatMap { listOf(it.date, it.date) }, selections)
        }
    }

    private fun assertSelectableDaysDoNotOverlap(days: List<ReadingCalendarDay>) {
        require(days.size >= 2)
        days.forEach { day ->
            composeTestRule
                .onNodeWithContentDescription(descriptionFor(day))
                .assertHasClickAction()
                .assertWidthIsAtLeast(ManiculeSize.touchTargetMin)
                .assertHeightIsAtLeast(ManiculeSize.touchTargetMin)
        }
        days.map { day ->
            composeTestRule.onNodeWithContentDescription(descriptionFor(day)).getUnclippedBoundsInRoot()
        }.zipWithNext().forEach { (previous, next) ->
            assertTrue("Targets must keep an 8dp gap", previous.bottom + ManiculeSpacing.sm <= next.top)
        }
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

    @Test
    fun partialWeeksKeepTheCorrectWeekdayRows() {
        val days = calendarDays(start = LocalDate(2026, 7, 8), count = 7)
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2026, 7, 20),
                    selectedDate = days.first().date,
                    modifier = Modifier.width(ManiculeSize.calendarCell * 2 + ManiculeSize.calendarCellGap),
                )
            }
        }
        val bounds = days.map { composeTestRule.onNodeWithContentDescription(descriptionFor(it)).getUnclippedBoundsInRoot() }
        val rowStep = bounds[6].top - bounds[5].top
        assertEquals((bounds[5].top + rowStep * 2).value, bounds[0].top.value, 0.01f)
        assertEquals((bounds[5].top + rowStep * 6).value, bounds[4].top.value, 0.01f)
        assertTrue(bounds[5].left > bounds[0].left)
        composeTestRule.onNodeWithContentDescription(descriptionFor(days.first())).assertIsSelected().assertHasNoClickAction()
    }

    @Test
    fun rangeChangesScrollToTheNewLatestWeek() {
        var days by mutableStateOf(calendarDays(count = 70))
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2030, 1, 1),
                    modifier = Modifier.width(ManiculeSize.calendarCell * 2),
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(descriptionFor(days.last())).assertIsDisplayed()
        composeTestRule.runOnIdle {
            days = calendarDays(start = LocalDate(2025, 12, 31), count = 90)
        }
        composeTestRule.onNodeWithContentDescription(descriptionFor(days.last())).assertIsDisplayed()
    }

    @Test
    fun selectionAndPageChangesPreserveTheWeekBeingViewed() {
        var days by mutableStateOf(calendarDays(count = 70))
        var selection by mutableStateOf<LocalDate?>(null)
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2030, 1, 1),
                    selectedDate = selection,
                    onDateSelected = { selection = it },
                    modifier = Modifier.width(ManiculeSize.touchTargetMin * 2),
                )
            }
        }
        composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)).performScrollToIndex(0)
        composeTestRule.onNodeWithContentDescription(descriptionFor(days.first())).performClick().assertIsSelected()
        composeTestRule.runOnIdle {
            days = days.map { ReadingCalendarDay.of(it.date, pages = 32) }
        }
        composeTestRule.onNodeWithContentDescription(descriptionFor(days.first())).assertIsDisplayed().assertIsSelected()
    }

    @Test
    fun enablingSelectionInCompactHeightPreservesTargetSizes() {
        val days = calendarDays(count = 7)
        var interactive by mutableStateOf(false)
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2026, 7, 20),
                    onDateSelected = if (interactive) ({}) else null,
                    modifier = Modifier.width(ManiculeSize.touchTargetMin).height(compactGridHeight),
                )
            }
        }
        composeTestRule.onNodeWithContentDescription(descriptionFor(days.first())).assertHasNoClickAction()
        composeTestRule.runOnIdle { interactive = true }
        assertSelectableDaysDoNotOverlap(days)
        composeTestRule.onNodeWithContentDescription(descriptionFor(days.last())).assertIsDisplayed()
    }

    @Test
    fun emptyCalendarCanReceiveAndClearDays() {
        val recordedDays = calendarDays(count = 7)
        var days by mutableStateOf(emptyList<ReadingCalendarDay>())
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2026, 7, 20),
                    modifier = Modifier.width(ManiculeSize.calendarCell),
                )
            }
        }
        composeTestRule.runOnIdle { days = recordedDays }
        composeTestRule.onNodeWithContentDescription(descriptionFor(recordedDays.last())).assertIsDisplayed()
        composeTestRule.runOnIdle { days = emptyList() }
        composeTestRule.onNodeWithContentDescription(descriptionFor(recordedDays.last())).assertDoesNotExist()
    }

    @Test
    fun shortCalendarShowsLatestDayAndSupportsVerticalSwipe() {
        val days = calendarDays(count = 70)
        var selectedDate: LocalDate? = null
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2030, 1, 1),
                    onDateSelected = { selectedDate = it },
                    contentPadding = PaddingValues(vertical = ManiculeSpacing.lg),
                    modifier = Modifier.width(ManiculeSize.touchTargetMin * 2).height(interactiveGridHeight / 2),
                )
            }
        }
        val sunday = composeTestRule.onNodeWithContentDescription(descriptionFor(days.last()))
        sunday.assertIsDisplayed()
        val viewport = composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
        viewport.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -10000f) }
        composeTestRule.onNodeWithContentDescription(descriptionFor(days[63])).assertIsDisplayed()
        repeat(3) { viewport.performTouchInput { swipeUp() } }
        sunday.assertIsDisplayed().performTouchInput { click() }
        composeTestRule.runOnIdle { assertEquals(days.last().date, selectedDate) }
    }

    @Test
    fun readOnlyWeekEdgeTodayRingsAreNotClipped() {
        val days = calendarDays(count = 7)
        var today by mutableStateOf(days.first().date)
        var ringColor = Color.Unspecified
        composeTestRule.setContent {
            ManiculeTheme {
                ringColor = MaterialTheme.colorScheme.primary
                Box(Modifier.testTag("calendar-frame").padding(ManiculeSpacing.sm)) {
                    ReadingCalendarGrid(days = days, today = today, modifier = Modifier.width(ManiculeSize.calendarCell))
                }
            }
        }

        listOf(days.first(), days.last()).forEach { day ->
            composeTestRule.runOnIdle { today = day.date }
            val description = context.getString(R.string.reading_calendar_today_content_description, descriptionFor(day))
            val dayBounds = composeTestRule.onNodeWithContentDescription(description).getUnclippedBoundsInRoot()
            val frame = composeTestRule.onNodeWithTag("calendar-frame")
            val frameBounds = frame.getUnclippedBoundsInRoot()
            val ringOffset = ManiculeSize.calendarTodayRingOffset + ManiculeBorder.ring / 2
            val ringY = if (day == days.first()) dayBounds.top - ringOffset else dayBounds.bottom + ringOffset
            val (x, y) = with(composeTestRule.density) {
                ((dayBounds.left + dayBounds.right) / 2 - frameBounds.left).toPx().roundToInt() to
                    (ringY - frameBounds.top).toPx().roundToInt()
            }
            assertEquals(ringColor.toArgb(), frame.captureToImage().toPixelMap()[x, y].toArgb())
        }
    }

    private fun calendarDays(
        start: LocalDate = LocalDate(2026, 7, 6),
        count: Int,
    ): List<ReadingCalendarDay> =
        (0 until count).map { offset ->
            ReadingCalendarDay.of(start.plus(DatePeriod(days = offset)), pages = 1)
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
