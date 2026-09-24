package com.leeseungyun1020.manicule.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.pressBack
import com.leeseungyun1020.manicule.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class SearchNavigationTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var books: NavigationBooks

    @Test
    fun selectedIsbn_opensBookDetailAndBackRestoresSearch() {
        hilt.inject()
        search("Navigation")
        compose.onNodeWithTag("search_results").performScrollToIndex(25)
        compose.onNodeWithText("Navigation book 24").performClick()
        compose.waitUntil { books.syncedIsbn == "isbn-24" }
        compose.onNodeWithText("Publication information").assertIsDisplayed()
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithTag("search_results").assertIsDisplayed()
        compose.onNodeWithText("Navigation book 24").assertIsDisplayed()
        compose.onNode(hasText("Navigation") and hasSetTextAction()).assertIsDisplayed()
        assertEquals(1, books.searchCalls)

        compose.onNodeWithText("Navigation book 24").performClick()
        compose.onNodeWithText("Publication information").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("Navigation book 24").assertIsDisplayed()
    }

    @Test
    fun emptySearch_opensScannerAndBackRestoresSearch() {
        search("Missing")
        compose.onNodeWithText("No search results").assertIsDisplayed()
        compose.onNodeWithText("Scan").assertIsDisplayed().performClick()
        compose.onNodeWithText("Scan barcode").assertIsDisplayed()
        pressBack()
        compose.onNodeWithText("No search results").assertIsDisplayed()
        compose.onNode(hasText("Missing") and hasSetTextAction()).assertIsDisplayed()
    }

    private fun search(query: String) {
        compose.onNodeWithText("Library").performClick()
        compose.onNodeWithText("Search").performClick()
        compose.onNode(hasSetTextAction()).apply {
            performTextInput(query)
            performImeAction()
        }
    }
}
