package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartBucket
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartUnit
import com.leeseungyun1020.manicule.feature.stats.components.ReadingChart
import com.leeseungyun1020.manicule.feature.stats.components.ReadingChartCard
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingChartTest {
    @get:Rule val composeRule = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun latestBucketIsInitiallyVisibleAndHasCompleteDescription() {
        val start = LocalDate(2025, 1, 1)
        val buckets = (0 until 120).map { index ->
            val date = start.plus(DatePeriod(days = index))
            ReadingChartBucket(date, date, index % 3, index.toLong())
        }
        composeRule.setContent {
            ManiculeTheme {
                ReadingChart(buckets, ChartKey(StatsPeriod.CUSTOM, start, buckets.last().end, ReadingChartUnit.DAY))
            }
        }
        val last = buckets.last()
        composeRule.onNodeWithContentDescription(
            context.getString(
                R.string.stats_chart_bucket_description,
                last.start.toString(),
                last.end.toString(),
                last.bookCount,
                last.pagesRead,
            ),
        ).assertIsDisplayed()
        val before = composeRule.onAllNodesWithText("0").fetchSemanticsNodes().map { it.boundsInRoot }
        composeRule.onNode(hasScrollAction()).performTouchInput { swipeRight() }
        val after = composeRule.onAllNodesWithText("0").fetchSemanticsNodes().map { it.boundsInRoot }
        org.junit.Assert.assertEquals(before, after)
    }

    @Test fun singleZeroBucketStillHasAccessibleValue() {
        val date = LocalDate(2026, 9, 1)
        composeRule.setContent {
            ManiculeTheme {
                ReadingChart(
                    listOf(ReadingChartBucket(date, date, 0, 0)),
                    ChartKey(StatsPeriod.CUSTOM, date, date, ReadingChartUnit.DAY),
                )
            }
        }
        composeRule.onNodeWithContentDescription(
            context.getString(R.string.stats_chart_bucket_description, date.toString(), date.toString(), 0, 0),
        ).assertIsDisplayed()
    }

    @Test fun cardUnitSelectionCallsBack() {
        val date = LocalDate(2026, 9, 1)
        var selected: ReadingChartUnit? = null
        composeRule.setContent {
            ManiculeTheme {
                ReadingChartCard(
                    state = ChartState.Content(
                        ChartKey(StatsPeriod.CUSTOM, date, date, ReadingChartUnit.DAY),
                        listOf(ReadingChartBucket(date, date, 1, 42)),
                    ),
                    unit = ReadingChartUnit.DAY,
                    onUnitSelected = { selected = it },
                    onRetry = {},
                )
            }
        }
        composeRule.onNodeWithTag("reading_chart_unit_week").performClick()
        composeRule.runOnIdle { org.junit.Assert.assertEquals(ReadingChartUnit.WEEK, selected) }
    }

    @Test fun chartUnitSelectorHasOneSelectionAndAccessibleTouchTargets() {
        val date = LocalDate(2026, 9, 1)
        var unit by mutableStateOf(ReadingChartUnit.DAY)
        composeRule.setContent {
            ManiculeTheme {
                ReadingChartCard(
                    state = ChartState.Content(
                        ChartKey(StatsPeriod.CUSTOM, date, date, ReadingChartUnit.DAY),
                        listOf(ReadingChartBucket(date, date, 1, 42)),
                    ),
                    unit = unit,
                    onUnitSelected = { unit = it },
                    onRetry = {},
                )
            }
        }
        val units = listOf("day", "week", "month")
        units.forEachIndexed { index, name ->
            val node = composeRule.onNodeWithTag("reading_chart_unit_$name")
            node.assertIsDisplayed()
            if (index == 0) node.assertIsSelected() else node.assertIsNotSelected()
            val bounds = node.fetchSemanticsNode().boundsInRoot
            val minTouchPx = with(composeRule.density) { 48.dp.toPx() }
            org.junit.Assert.assertTrue("$name width=${bounds.width}", bounds.width >= minTouchPx)
            org.junit.Assert.assertTrue("$name height=${bounds.height}", bounds.height >= minTouchPx)
        }
        composeRule.onNodeWithText(context.getString(R.string.stats_chart_day_short)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_chart_week_short)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.stats_chart_month_short)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(R.string.stats_chart_week_description)).performClick()
        composeRule.onNodeWithTag("reading_chart_unit_week").assertIsSelected()
        composeRule.onNodeWithTag("reading_chart_unit_day").assertIsNotSelected()
        composeRule.onNodeWithTag("reading_chart_unit_month").assertIsNotSelected()
        composeRule.runOnIdle { org.junit.Assert.assertEquals(ReadingChartUnit.WEEK, unit) }
    }

    @Test fun chartHeaderWrapsOnlyWhenMeasuredContentDoesNotFit() {
        val date = LocalDate(2026, 9, 1)
        var width by mutableStateOf(240.dp)
        var fontScale by mutableStateOf(1f)
        var direction by mutableStateOf(LayoutDirection.Ltr)
        composeRule.setContent {
            ManiculeTheme {
                val density = LocalDensity.current
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density, fontScale),
                    LocalLayoutDirection provides direction,
                ) {
                    Box(Modifier.width(width)) {
                        ReadingChartCard(
                            state = ChartState.Content(
                                ChartKey(StatsPeriod.CUSTOM, date, date, ReadingChartUnit.DAY),
                                listOf(ReadingChartBucket(date, date, 1, 42)),
                            ),
                            unit = ReadingChartUnit.DAY,
                            onUnitSelected = {},
                            onRetry = {},
                        )
                    }
                }
            }
        }
        val title = composeRule.onNodeWithTag("reading_chart_title")
        val selector = composeRule.onNodeWithTag("reading_chart_unit_selector")
        org.junit.Assert.assertTrue(selector.fetchSemanticsNode().boundsInRoot.top >= title.fetchSemanticsNode().boundsInRoot.bottom)
        composeRule.runOnIdle { width = 600.dp }
        org.junit.Assert.assertTrue(selector.fetchSemanticsNode().boundsInRoot.top < title.fetchSemanticsNode().boundsInRoot.bottom)
        org.junit.Assert.assertTrue(selector.fetchSemanticsNode().boundsInRoot.left >= title.fetchSemanticsNode().boundsInRoot.right)
        composeRule.runOnIdle {
            width = 320.dp
            fontScale = 2f
        }
        org.junit.Assert.assertTrue(selector.fetchSemanticsNode().boundsInRoot.top >= title.fetchSemanticsNode().boundsInRoot.bottom)
        composeRule.runOnIdle {
            width = 600.dp
            fontScale = 1f
            direction = LayoutDirection.Rtl
        }
        org.junit.Assert.assertTrue(selector.fetchSemanticsNode().boundsInRoot.right <= title.fetchSemanticsNode().boundsInRoot.left)
    }

    @Test fun rtlKeepsBookAxisOnPhysicalLeft() {
        val date = LocalDate(2026, 9, 1)
        composeRule.setContent {
            ManiculeTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ReadingChart(
                        listOf(ReadingChartBucket(date, date, 4, 80)),
                        ChartKey(StatsPeriod.CUSTOM, date, date, ReadingChartUnit.DAY),
                    )
                }
            }
        }
        val bookAxisX = composeRule.onNodeWithText("4").fetchSemanticsNode().boundsInRoot.center.x
        val pageAxisX = composeRule.onNodeWithText("80").fetchSemanticsNode().boundsInRoot.center.x
        org.junit.Assert.assertTrue(bookAxisX < pageAxisX)
    }

    @Test fun enlargedLabelsFitTheirSlotsAndAxes() {
        val months = (1..12).map { month ->
            val date = LocalDate(2026, month, 1)
            ReadingChartBucket(date, date, 4, 80)
        }
        composeRule.setContent {
            ManiculeTheme {
                val density = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                    ReadingChart(
                        months,
                        ChartKey(StatsPeriod.ONE_YEAR, months.first().start, months.last().end, ReadingChartUnit.MONTH),
                    )
                }
            }
        }
        val lastLabel = composeRule.onNodeWithText("2026/12")
        lastLabel.assertIsDisplayed()
        val layouts = mutableListOf<TextLayoutResult>()
        lastLabel.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
        val layout = layouts.single()
        org.junit.Assert.assertFalse(
            "label size=${layout.size}, paragraph width=${layout.multiParagraph.width}, height=${layout.multiParagraph.height}, " +
                "line count=${layout.lineCount}",
            layout.hasVisualOverflow,
        )
        val bottomAxis = composeRule.onAllNodesWithText("0").fetchSemanticsNodes().maxOf { it.boundsInRoot.bottom }
        val xLabelTop = lastLabel.fetchSemanticsNode().boundsInRoot.top
        org.junit.Assert.assertTrue(bottomAxis < xLabelTop)
    }

    @Test fun newKeyStartsAtLatestAfterOuterListDisposesChart() {
        val monthStart = LocalDate(2025, 1, 1)
        val monthly = (0 until 12).map { index ->
            val date = monthStart.plus(DatePeriod(months = index))
            ReadingChartBucket(date, date, 1, 10)
        }
        val dayStart = LocalDate(2026, 1, 1)
        val daily = (0 until 120).map { index ->
            val date = dayStart.plus(DatePeriod(days = index))
            ReadingChartBucket(date, date, 1, 10)
        }
        var currentBuckets by mutableStateOf(monthly)
        var currentKey by mutableStateOf(
            ChartKey(StatsPeriod.ONE_YEAR, monthly.first().start, monthly.last().end, ReadingChartUnit.MONTH),
        )
        val outerState = LazyListState()
        composeRule.setContent {
            ManiculeTheme {
                LazyColumn(state = outerState, modifier = androidx.compose.ui.Modifier.fillMaxSize().testTag("outer_list")) {
                    item(key = "chart") { ReadingChart(currentBuckets, currentKey) }
                    item(key = "filler") { Spacer(androidx.compose.ui.Modifier.height(2000.dp)) }
                }
            }
        }
        composeRule.onNodeWithTag("reading_chart_row").performScrollToIndex(0)
        composeRule.onNodeWithTag("outer_list").performScrollToIndex(1)
        composeRule.onNodeWithTag("reading_chart_row").assertDoesNotExist()
        composeRule.runOnIdle {
            currentBuckets = daily
            currentKey = ChartKey(StatsPeriod.CUSTOM, daily.first().start, daily.last().end, ReadingChartUnit.DAY)
        }
        composeRule.onNodeWithTag("outer_list").performScrollToIndex(0)
        val last = daily.last()
        composeRule.onNodeWithContentDescription(
            context.getString(R.string.stats_chart_bucket_description, last.start.toString(), last.end.toString(), 1, 10),
        ).assertIsDisplayed()
    }
}
