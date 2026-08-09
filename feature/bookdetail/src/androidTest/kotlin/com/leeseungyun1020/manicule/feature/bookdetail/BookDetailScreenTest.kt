package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.Book
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
                    uiState = BookDetailUiState(book = testBook, isLoading = false),
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
                    uiState = BookDetailUiState(isLoading = false, isFatalError = true),
                    onNavigateBack = {},
                    onTabSelected = {},
                    onRetry = { retried = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.book_detail_retry)).performClick()

        assertThat(retried).isTrue()
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
    }
}
