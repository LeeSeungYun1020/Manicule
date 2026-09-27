package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight
import androidx.compose.ui.unit.LayoutDirection
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
        composeRule.onNodeWithText(context.getString(R.string.stats_chart_week)).performClick()
        composeRule.runOnIdle { org.junit.Assert.assertEquals(ReadingChartUnit.WEEK, selected) }
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
}
