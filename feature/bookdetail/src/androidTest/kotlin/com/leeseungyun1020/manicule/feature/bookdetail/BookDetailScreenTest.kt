package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookDetail
import com.leeseungyun1020.manicule.feature.bookdetail.components.BookDetailExpandableText
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

    private companion object {
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
