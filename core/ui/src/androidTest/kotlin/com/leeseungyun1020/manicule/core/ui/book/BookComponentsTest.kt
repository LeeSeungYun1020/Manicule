package com.leeseungyun1020.manicule.core.ui.book

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookComponentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bookCover_appliesRequestedSize() {
        composeTestRule.setContent {
            ManiculeTheme {
                Row {
                    BookCover(
                        imageUrl = null,
                        modifier = Modifier.testTag("small-cover"),
                        size = BookCoverSize.Small,
                    )
                    BookCover(
                        imageUrl = null,
                        modifier = Modifier.testTag("medium-cover"),
                        size = BookCoverSize.Medium,
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag("small-cover")
            .assertWidthIsEqualTo(BookCoverSize.Small.width)
            .assertHeightIsEqualTo(BookCoverSize.Small.height)
        composeTestRule
            .onNodeWithTag("medium-cover")
            .assertWidthIsEqualTo(BookCoverSize.Medium.width)
            .assertHeightIsEqualTo(BookCoverSize.Medium.height)
    }

    @Test
    fun bookProgressBar_displaysPagesAndPercentage() {
        composeTestRule.setContent {
            ManiculeTheme {
                BookProgressBar(currentPage = 132, totalPages = 320)
            }
        }

        composeTestRule.onNodeWithText("132 / 320").assertIsDisplayed()
        composeTestRule.onNodeWithText("41%").assertIsDisplayed()
    }

    @Test
    fun bookProgressBar_clampsPercentageToValidRange() {
        composeTestRule.setContent {
            ManiculeTheme {
                BookProgressBar(currentPage = 400, totalPages = 320)
            }
        }

        composeTestRule.onNodeWithText("100%").assertIsDisplayed()
    }

    @Test
    fun bookListItem_rendersTrailingContentWhenProvided() {
        composeTestRule.setContent {
            ManiculeTheme {
                BookListItem(
                    title = "Test Book",
                    author = "Test Author",
                    publisher = "Test Publisher",
                    pubDate = "2026.01.01",
                    imageUrl = null,
                    trailingContent = { Text("120p") },
                )
            }
        }

        composeTestRule.onNodeWithText("120p").assertIsDisplayed()
    }
}
