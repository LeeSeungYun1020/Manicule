package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookDetail
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.bookdetail.components.BookDetailExpandableText
import kotlinx.datetime.Instant
import org.junit.Rule
import org.junit.Test
import com.leeseungyun1020.manicule.core.designsystem.R as DesignSystemR

class BookDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun informationTab_displaysBibliographicFields_andChangesTab() {
        var selectedTab: BookDetailTab? = null
        var navigatedBack = false
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = contentState(),
                    onNavigateBack = { navigatedBack = true },
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = { selectedTab = it },
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText("Author").assertIsDisplayed()
        composeRule.onNodeWithText("Publisher").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_tab_my_records)).performClick()
        composeRule.onNodeWithContentDescription(context.getString(DesignSystemR.string.core_designsystem_back)).performClick()

        assertThat(selectedTab).isEqualTo(BookDetailTab.MyRecords)
        assertThat(navigatedBack).isTrue()
    }

    @Test
    fun fatalError_displaysRetry() {
        var retried = false
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = BookDetailUiState.Error,
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = { retried = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_retry)).performClick()

        assertThat(retried).isTrue()
    }

    @Test
    fun refreshFailure_keepsContent_andRetryDismissesSnackbar() {
        var retried = false
        var uiState by mutableStateOf(contentState(refreshStatus = RefreshStatus.Failed))
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onStatusSelected = {},
                    onStatusErrorDismissed = {},
                    onTabSelected = {},
                    onRetry = {
                        retried = true
                        uiState = uiState.copy(refreshStatus = RefreshStatus.Refreshing)
                    },
                )
            }
        }

        val refreshError = context.getString(R.string.book_detail_refresh_error)
        composeRule.onNodeWithText("Author").assertIsDisplayed()
        composeRule.onNodeWithText(refreshError).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_retry)).performClick()

        assertThat(retried).isTrue()
        composeRule.onAllNodesWithText(refreshError).assertCountEquals(0)
        composeRule.onNodeWithText("Author").assertIsDisplayed()
    }

    @Test
    fun expandableText_preservesExpandedStateAfterRestoration() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent {
            ManiculeTheme {
                BookDetailExpandableText(
                    title = "Introduction",
                    text = "Long content ".repeat(100),
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_expand)).performClick()
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithText(context.getString(R.string.book_detail_collapse)).assertIsDisplayed()
    }

    @Test
    fun unregisteredAndReviewOnly_showNoSelection_andRegisteredStatesMatchDatabase() {
        var uiState by mutableStateOf(recordsState(null))
        var requested: ReadingStatus? = null
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(uiState, {}, {}, {}, { requested = it }, {})
            }
        }
        statusLabels.forEach { composeRule.onNodeWithText(context.getString(it)).assertIsNotSelected() }
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_want)).performClick()
        assertThat(requested).isEqualTo(ReadingStatus.WANT)
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_want)).assertIsNotSelected()
        composeRule.runOnIdle { uiState = recordsState(ReadingStatus.UNSET) }
        statusLabels.forEach { composeRule.onNodeWithText(context.getString(it)).assertIsNotSelected() }
        listOf(ReadingStatus.WANT, ReadingStatus.READING, ReadingStatus.FINISHED).forEachIndexed { index, status ->
            composeRule.runOnIdle { uiState = recordsState(status) }
            statusLabels.forEachIndexed { labelIndex, label ->
                val node = composeRule.onNodeWithText(context.getString(label))
                if (index == labelIndex) node.assertIsSelected() else node.assertIsNotSelected()
            }
        }
    }

    @Test
    fun saving_disablesEveryStatus_andKeepsPersistedSelection() {
        var requested = false
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    recordsState(ReadingStatus.READING).copy(statusChange = StatusChangeState.Saving(ReadingStatus.FINISHED)),
                    {},
                    {},
                    {},
                    { requested = true },
                    {},
                )
            }
        }
        statusLabels.forEach {
            composeRule.onNodeWithText(context.getString(it)).assertIsNotEnabled().performClick()
        }
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_reading)).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_saving)).assertIsDisplayed()
        assertThat(requested).isFalse()
    }

    @Test
    fun saveError_takesPriorityOverRefreshError_andRetriesRequestedStatus() {
        var uiState by mutableStateOf(recordsState(ReadingStatus.READING).copy(refreshStatus = RefreshStatus.Failed))
        var retriedStatus: ReadingStatus? = null
        var refreshed = false
        composeRule.setContent {
            ManiculeTheme {
                BookDetailScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onTabSelected = {},
                    onRetry = { refreshed = true },
                    onStatusSelected = {
                        retriedStatus = it
                        uiState = uiState.copy(statusChange = StatusChangeState.Saving(it))
                    },
                    onStatusErrorDismissed = {},
                )
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.book_detail_refresh_error)).assertIsDisplayed()
        composeRule.runOnIdle { uiState = uiState.copy(statusChange = StatusChangeState.Failed(ReadingStatus.FINISHED, 1)) }
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_error)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_reading)).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.book_detail_retry)).performClick()
        assertThat(retriedStatus).isEqualTo(ReadingStatus.FINISHED)
        assertThat(refreshed).isFalse()
        composeRule.onAllNodesWithText(context.getString(R.string.book_detail_status_error)).assertCountEquals(0)
        composeRule.onNodeWithText(context.getString(R.string.book_detail_status_saving)).assertIsDisplayed()
    }

    @Test
    fun largeFont_keepsSegmentHeightsAligned_whenLabelWraps() {
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.5f)) {
                ManiculeTheme {
                    BookDetailScreen(recordsState(null), {}, {}, {}, {}, {})
                }
            }
        }
        val heights = statusLabels.map {
            composeRule.onNodeWithText(context.getString(it)).assertIsDisplayed().fetchSemanticsNode().boundsInRoot.height
        }
        heights.forEach { assertThat(it).isWithin(1f).of(heights.first()) }
    }

    private companion object {
        val statusLabels =
            listOf(R.string.book_detail_status_want, R.string.book_detail_status_reading, R.string.book_detail_status_finished)

        fun recordsState(status: ReadingStatus?) =
            contentState().copy(
                selectedTab = BookDetailTab.MyRecords,
                bookDetail = BookDetail(
                    testBook,
                    status?.let {
                        BookEntry(
                            testBook,
                            it,
                            rating = 4,
                            memo = "Keep review",
                            addedAt = Instant.fromEpochMilliseconds(1),
                            updatedAt = Instant.fromEpochMilliseconds(1),
                        )
                    },
                ),
            )

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val testBook =
            Book(
                isbn = "123",
                title = "Test Book",
                author = "Author",
                publisher = "Publisher",
                publishedDate = null,
                coverUrl = null,
                totalPages = 300,
                price = 20_000,
                category = "Category",
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
                introduction = "Introduction",
                tableOfContents = "Contents",
            )

        fun contentState(refreshStatus: RefreshStatus = RefreshStatus.Idle) =
            BookDetailUiState.Content(
                bookDetail = BookDetail(testBook, entry = null),
                selectedTab = BookDetailTab.Information,
                refreshStatus = refreshStatus,
            )
    }
}
