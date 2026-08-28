package com.leeseungyun1020.manicule.feature.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SearchScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_requestsInitialSearchFocusAndNavigatesBack() {
        var navigatedBack = false
        composeTestRule.setSearchContent(
            uiState = SearchUiState.Content(emptyList()),
            onNavigateBack = { navigatedBack = true },
        )

        composeTestRule.onNodeWithText("What book are you looking for?").assertIsDisplayed()
        composeTestRule
            .onNode(hasText("Search by title, author, or ISBN") and hasSetTextAction())
            .assertIsFocused()
        composeTestRule.onNodeWithContentDescription("Navigate back").performClick()

        assertThat(navigatedBack).isTrue()
    }

    @Test
    fun recentQueries_areDisplayed() {
        composeTestRule.setSearchContent(
            uiState = SearchUiState.Content(listOf("Compose", "A long recent search query")),
        )

        composeTestRule.onNodeWithText("Recent searches").assertIsDisplayed()
        composeTestRule.onNodeWithText("Compose").assertIsDisplayed()
        composeTestRule.onNodeWithText("A long recent search query").assertIsDisplayed()
    }

    @Test
    fun loadingState_isAccessible() {
        composeTestRule.setSearchContent(uiState = SearchUiState.Loading)

        composeTestRule.onNodeWithContentDescription("Loading recent searches").assertIsDisplayed()
    }

    @Test
    fun unavailableState_displaysEmptyStateAndKeepsSearchFocused() {
        composeTestRule.setSearchContent(uiState = SearchUiState.Unavailable)

        composeTestRule.onNodeWithText("What book are you looking for?").assertIsDisplayed()
        composeTestRule
            .onNode(hasText("Search by title, author, or ISBN") and hasSetTextAction())
            .assertIsFocused()
    }

    @Test
    fun content_isDisplayedAtCompactWidth() {
        composeTestRule.setSearchContent(
            uiState = SearchUiState.Content(listOf("Compact")),
            width = 320,
        )

        composeTestRule.onNodeWithText("Compact").assertIsDisplayed()
    }

    @Test
    fun content_isDisplayedAtExpandedWidth() {
        composeTestRule.setSearchContent(
            uiState = SearchUiState.Content(listOf("Expanded")),
            width = 840,
        )

        composeTestRule.onNodeWithText("Expanded").assertIsDisplayed()
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setSearchContent(
    uiState: SearchUiState,
    onNavigateBack: () -> Unit = {},
    width: Int? = null,
) {
    setContent {
        ManiculeTheme {
            Box(modifier = width?.let { Modifier.width(it.dp) } ?: Modifier) {
                SearchScreen(
                    uiState = uiState,
                    searchFieldState = rememberTextFieldState(),
                    onNavigateBack = onNavigateBack,
                )
            }
        }
    }
}
