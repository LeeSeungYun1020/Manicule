package com.leeseungyun1020.manicule.core.ui.calendar

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.UiAutomation
import android.content.Context
import android.graphics.Bitmap
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.foundation.background
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.swipe
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
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
import java.io.File
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
                            .height(compactGridHeight),
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(descriptionFor(day))
            .assertHasClickAction()
            .assertWidthIsEqualTo(20.dp)
            .assertHeightIsEqualTo(20.dp)
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
                            .height(compactGridHeight),
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
            .assertHeightIsEqualTo(compactGridHeight + ManiculeSpacing.lg * 2)
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
            .assertHeightIsEqualTo(compactGridHeight + ManiculeSpacing.lg * 2)
        assertSelectableDaysDoNotOverlap(days)
    }

    @Test
    fun fixedHeightKeepsDayTargetsAndAllowsScrollingToSunday() {
        assertConstrainedCalendar(Modifier.height(compactGridHeight / 2))
    }

    @Test
    fun fixedHeightWithBothPaddingsKeepsDayTargets() {
        assertConstrainedCalendar(
            modifier = Modifier.height(compactGridHeight / 2).padding(vertical = ManiculeSpacing.lg),
            contentPadding = PaddingValues(ManiculeSpacing.lg),
        )
    }

    @Test
    fun paddingBeforeFixedHeightKeepsDayTargets() {
        assertConstrainedCalendar(
            modifier = Modifier.padding(vertical = ManiculeSpacing.lg).height(compactGridHeight / 2),
            contentPadding = PaddingValues(vertical = ManiculeSpacing.lg),
        )
    }

    @Test
    fun parentHeightLimitWithBothPaddingsKeepsDayTargets() {
        assertConstrainedCalendar(
            modifier = Modifier.padding(vertical = ManiculeSpacing.lg),
            contentPadding = PaddingValues(vertical = ManiculeSpacing.lg),
            parentHeight = compactGridHeight / 2,
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
        composeTestRule.onNodeWithTag(TEST_CALENDAR_TAG).assertHeightIsEqualTo(compactGridHeight)
        assertSelectableDaysDoNotOverlap(days)
    }

    private fun assertConstrainedCalendar(
        modifier: Modifier,
        contentPadding: PaddingValues = PaddingValues(),
        parentHeight: Dp = compactGridHeight,
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
                .assertWidthIsEqualTo(20.dp)
                .assertHeightIsEqualTo(20.dp)
        }
        days.map { day ->
            composeTestRule.onNodeWithContentDescription(descriptionFor(day)).getUnclippedBoundsInRoot()
        }.zipWithNext().forEach { (previous, next) ->
            assertEquals(with(composeTestRule.density) { 4.dp.roundToPx().toDp().value }, (next.top - previous.bottom).value, 0.001f)
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
        val selections = mutableListOf<LocalDate>()
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2030, 1, 1),
                    onDateSelected = selections::add,
                    contentPadding = PaddingValues(vertical = ManiculeSpacing.lg),
                    modifier = Modifier.width(ManiculeSize.touchTargetMin * 2).height(compactGridHeight / 2),
                )
            }
        }
        val sunday = composeTestRule.onNodeWithContentDescription(descriptionFor(days.last()))
        sunday.assertIsDisplayed()
        val viewport = composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange))
        viewport.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, -10000f) }
        composeTestRule.onNodeWithContentDescription(descriptionFor(days[63])).assertIsDisplayed()
        repeat(3) { viewport.performTouchInput { swipeUp() } }
        composeTestRule.runOnIdle { assertTrue(selections.isEmpty()) }
        sunday.assertIsDisplayed().performTouchInput { click() }
        composeTestRule.runOnIdle { assertEquals(listOf(days.last().date), selections) }
    }

    @Test
    fun weekEdgeTodayRingsStayInsideCells() {
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
            val ringOffset = ManiculeBorder.ring / 2
            val ringY = if (day == days.first()) dayBounds.top + ringOffset else dayBounds.bottom - ringOffset
            val (x, y) = with(composeTestRule.density) {
                ((dayBounds.left + dayBounds.right) / 2 - frameBounds.left).toPx().roundToInt() to
                    (ringY - frameBounds.top).toPx().roundToInt()
            }
            assertEquals(ringColor.toArgb(), frame.captureToImage().toPixelMap()[x, y].toArgb())
        }
    }

    @Test
    fun tapsSelectOnlyTheTouchedCellAndIgnoreGapsPaddingAndDisabledDays() {
        val days = calendarDays(start = LocalDate(2026, 7, 8), count = 10).mapIndexed { index, day ->
            ReadingCalendarDay.of(day.date, pages = if (index == 2) 0 else 1)
        }
        val selections = mutableListOf<LocalDate>()
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2030, 1, 1),
                    isDateSelectable = { it.pages > 0 && it.date != days[3].date },
                    onDateSelected = selections::add,
                    modifier = Modifier.width(200.dp).testTag(TEST_CALENDAR_TAG).padding(16.dp),
                    contentPadding = PaddingValues(16.dp),
                )
            }
        }
        val frame = composeTestRule.onNodeWithTag(TEST_CALENDAR_TAG)
        val origin = frame.getUnclippedBoundsInRoot()
        val first = composeTestRule.onNodeWithContentDescription(descriptionFor(days[0])).getUnclippedBoundsInRoot()
        val nextWeek = composeTestRule.onNodeWithContentDescription(descriptionFor(days[5])).getUnclippedBoundsInRoot()

        fun tapAt(
            x: Dp,
            y: Dp,
        ) {
            val point = with(composeTestRule.density) { Offset((x - origin.left).toPx(), (y - origin.top).toPx()) }
            frame.performTouchInput { click(point) }
        }
        tapAt(origin.left + 2.dp, origin.top + 2.dp)
        tapAt(first.left - 8.dp, first.top + 10.dp)
        tapAt(first.left + 10.dp, first.bottom + 2.dp)
        tapAt(first.right + 2.dp, first.top + 10.dp)
        tapAt(first.left + 10.dp, nextWeek.top + 10.dp) // 범위 시작 전 월요일
        tapAt(nextWeek.left + 10.dp, nextWeek.top + 24.dp * 6 + 10.dp) // 범위 종료 후 일요일
        listOf(days[2], days[3]).forEach { day ->
            composeTestRule.onNodeWithContentDescription(descriptionFor(day))
                .assertHasNoClickAction().performTouchInput {
                    click(center)
                    click(topLeft + Offset(1f, 1f))
                    click(bottomRight - Offset(1f, 1f))
                }
        }
        composeTestRule.runOnIdle { assertTrue(selections.isEmpty()) }
        val selectable = days.filter { it.pages > 0 && it.date != days[3].date }
        selectable.forEach { day ->
            composeTestRule.onNodeWithContentDescription(descriptionFor(day)).performTouchInput {
                click(center)
                click(topLeft + Offset(1f, 1f))
                click(bottomRight - Offset(1f, 1f))
            }
        }
        composeTestRule.runOnIdle { assertEquals(selectable.flatMap { listOf(it.date, it.date, it.date) }, selections) }
    }

    @Test
    fun enablingSelectionKeepsLayoutAndTheViewedWeek() {
        val days = calendarDays(count = 70)
        var interactive by mutableStateOf(false)
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(
                    days = days,
                    today = LocalDate(2030, 1, 1),
                    onDateSelected = if (interactive) ({}) else null,
                    modifier = Modifier.width(68.dp).testTag(TEST_CALENDAR_TAG),
                )
            }
        }
        composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange)).performScrollToIndex(0)
        val first = composeTestRule.onNodeWithContentDescription(descriptionFor(days.first()))
        val before = first.getUnclippedBoundsInRoot()
        val gridBefore = composeTestRule.onNodeWithTag(TEST_CALENDAR_TAG).getUnclippedBoundsInRoot()
        composeTestRule.runOnIdle { interactive = true }
        first.assertHasClickAction().assertIsDisplayed()
        assertEquals(before, first.getUnclippedBoundsInRoot())
        assertEquals(gridBefore, composeTestRule.onNodeWithTag(TEST_CALENDAR_TAG).getUnclippedBoundsInRoot())
        composeTestRule.onNodeWithTag(TEST_CALENDAR_TAG).assertHeightIsEqualTo(compactGridHeight)
    }

    @Test
    fun rtlChronologyLatestDateAndSwipeUseReadingDirectionWithoutSelecting() {
        val days = calendarDays(count = 70)
        var direction by mutableStateOf(LayoutDirection.Ltr)
        val selections = mutableListOf<LocalDate>()
        composeTestRule.setContent {
            ManiculeTheme {
                key(direction) {
                    CompositionLocalProvider(LocalLayoutDirection provides direction) {
                        ReadingCalendarGrid(
                            days = days,
                            today = LocalDate(2030, 1, 1),
                            onDateSelected = selections::add,
                            modifier = Modifier.width(68.dp),
                        )
                    }
                }
            }
        }
        listOf(LayoutDirection.Ltr, LayoutDirection.Rtl).forEach { layoutDirection ->
            composeTestRule.runOnIdle { direction = layoutDirection }
            val scroll = composeTestRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsProperties.HorizontalScrollAxisRange))
            composeTestRule.onNodeWithContentDescription(descriptionFor(days.last())).assertIsDisplayed()
            scroll.performScrollToIndex(0)
            val first = composeTestRule.onNodeWithContentDescription(descriptionFor(days[0])).getUnclippedBoundsInRoot()
            val second = composeTestRule.onNodeWithContentDescription(descriptionFor(days[7])).getUnclippedBoundsInRoot()
            assertTrue(if (layoutDirection == LayoutDirection.Ltr) first.left < second.left else first.left > second.left)
            scroll.performTouchInput {
                val startX = if (layoutDirection == LayoutDirection.Ltr) width - 10.dp.toPx() else 10.dp.toPx()
                val endX = if (layoutDirection == LayoutDirection.Ltr) 10.dp.toPx() else width - 10.dp.toPx()
                swipe(start = Offset(startX, 10.dp.toPx()), end = Offset(endX, 10.dp.toPx()), durationMillis = 500)
            }
            composeTestRule.waitForIdle()
            composeTestRule.runOnIdle { assertTrue(selections.isEmpty()) }
            assertTrue(scroll.fetchSemanticsNode().config[SemanticsProperties.HorizontalScrollAxisRange].value() > 0f)
        }
    }

    @OptIn(androidx.compose.ui.test.ExperimentalTestApi::class)
    @Test
    fun keyboardAndAccessibilityActionsSelectTheFocusedDate() {
        val days = calendarDays(count = 7)
        val selections = mutableListOf<LocalDate>()
        lateinit var inputModeManager: InputModeManager
        composeTestRule.setContent {
            ManiculeTheme {
                inputModeManager = LocalInputModeManager.current
                ReadingCalendarGrid(days = days, today = LocalDate(2030, 1, 1), onDateSelected = selections::add)
            }
        }
        val first = composeTestRule.onNodeWithContentDescription(descriptionFor(days[0]))
        composeTestRule.runOnIdle { assertTrue(inputModeManager.requestInputMode(InputMode.Keyboard)) }
        first.performSemanticsAction(SemanticsActions.RequestFocus) { assertTrue(it()) }
        first.assertIsFocused()
        first.performKeyInput { pressKey(Key.Enter) }
        composeTestRule.onNodeWithContentDescription(descriptionFor(days[1])).performSemanticsAction(SemanticsActions.OnClick) { it() }
        composeTestRule.runOnIdle { assertEquals(listOf(days[0].date, days[1].date), selections) }
    }

    @Test
    fun adjacentTodayAndSelectedRingsStayInsideCellsInBothThemes() {
        val days = calendarDays(count = 14)
        var dark by mutableStateOf(false)
        var today by mutableStateOf(days[1].date)
        var todayColor = Color.Unspecified
        var selectedColor = Color.Unspecified
        var backgroundColor = Color.Unspecified
        composeTestRule.setContent {
            ManiculeTheme(darkTheme = dark) {
                todayColor = MaterialTheme.colorScheme.primary
                selectedColor = MaterialTheme.colorScheme.tertiary
                backgroundColor = MaterialTheme.colorScheme.background
                Box(Modifier.width(120.dp).background(backgroundColor).testTag("rings").padding(8.dp)) {
                    ReadingCalendarGrid(
                        days = days,
                        today = today,
                        selectedDate = days[0].date,
                        onDateSelected = {},
                    )
                }
            }
        }
        listOf(false, true).forEach { darkTheme ->
            composeTestRule.runOnIdle { dark = darkTheme }
            val frame = composeTestRule.onNodeWithTag("rings")
            val frameBounds = frame.getUnclippedBoundsInRoot()
            val screenshot = frame.captureToImage()
            val pixels = screenshot.toPixelMap()

            fun colorAt(
                x: Dp,
                y: Dp,
            ): Int {
                val px = with(composeTestRule.density) { (x - frameBounds.left).toPx().roundToInt() }
                val py = with(composeTestRule.density) { (y - frameBounds.top).toPx().roundToInt() }
                return pixels[px, py].toArgb()
            }
            listOf(0, 1).forEach { index ->
                val description = if (index ==
                    1
                ) {
                    context.getString(R.string.reading_calendar_today_content_description, descriptionFor(days[index]))
                } else {
                    descriptionFor(days[index])
                }
                val bounds = composeTestRule.onNodeWithContentDescription(description).getUnclippedBoundsInRoot()
                val x = (bounds.left + bounds.right) / 2
                assertEquals((if (index == 0) selectedColor else todayColor).toArgb(), colorAt(x, bounds.top + 1.dp))
                assertEquals(backgroundColor.toArgb(), colorAt(x, bounds.top - 1.dp))
                assertEquals(backgroundColor.toArgb(), colorAt(x, bounds.bottom + 1.dp))
            }
            File(context.cacheDir, "calendar-${if (darkTheme) "dark" else "light"}.png").outputStream().use {
                screenshot.asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
            }
        }
        composeTestRule.runOnIdle { today = days[0].date }
        val first = composeTestRule.onNodeWithContentDescription(
            context.getString(R.string.reading_calendar_today_content_description, descriptionFor(days[0])),
        )
        first.assertIsSelected()
        val bitmap = first.captureToImage().toPixelMap()
        assertEquals(selectedColor.toArgb(), bitmap[bitmap.width / 2, with(composeTestRule.density) { 1.dp.toPx().roundToInt() }].toArgb())
    }

    @Test
    fun accessibilityServiceCanFocusAndActivateADate() {
        val days = calendarDays(count = 7)
        val selections = mutableListOf<LocalDate>()
        composeTestRule.setContent {
            ManiculeTheme {
                ReadingCalendarGrid(days = days, today = LocalDate(2030, 1, 1), onDateSelected = selections::add)
            }
        }
        val automation = InstrumentationRegistry.getInstrumentation().getUiAutomation(
            UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES,
        )
        val description = descriptionFor(days[1])

        fun findDateNode(node: AccessibilityNodeInfo?): AccessibilityNodeInfo? {
            if (node == null || node.contentDescription?.toString() == description) return node
            repeat(node.childCount) { index ->
                findDateNode(node.getChild(index))?.let { return it }
            }
            return null
        }
        val originalServiceInfo = automation.serviceInfo
        try {
            automation.serviceInfo = automation.serviceInfo.apply {
                flags = flags or AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE
            }
            val accessibilityManager = context.getSystemService(AccessibilityManager::class.java)
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                accessibilityManager.isTouchExplorationEnabled && findDateNode(automation.rootInActiveWindow) != null
            }
            val node = checkNotNull(findDateNode(automation.rootInActiveWindow))
            node.performAction(AccessibilityNodeInfo.ACTION_CLEAR_ACCESSIBILITY_FOCUS)
            assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS))
            assertTrue(node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
            composeTestRule.runOnIdle { assertEquals(listOf(days[1].date), selections) }
        } finally {
            automation.serviceInfo = originalServiceInfo
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

    private val compactGridHeight: Dp
        get() = with(composeTestRule.density) {
            // 20dp 셀과 4dp 간격은 각각 물리 픽셀로 반올림되어 배치된다.
            (20.dp.roundToPx() * TEST_CALENDAR_ROW_COUNT + 4.dp.roundToPx() * TEST_CALENDAR_GAP_COUNT).toDp()
        }
}
