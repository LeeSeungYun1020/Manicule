package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TopNavigationComponentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun searchBar_acceptsInputAndSubmitsSearch() {
        var submittedQuery = ""
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeSearchBar(
                    state = rememberTextFieldState(),
                    onSearch = { submittedQuery = it },
                    placeholder = "Search books",
                )
            }
        }

        composeTestRule.onNode(hasSetTextAction()).performTextInput("Almond")
        composeTestRule.onNode(hasSetTextAction()).assertTextContains("Almond").performImeAction()

        composeTestRule.runOnIdle { assertEquals("Almond", submittedQuery) }
    }

    @Test
    fun searchBar_requestsFocusWhenAutoFocusIsEnabled() {
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeSearchBar(
                    state = rememberTextFieldState(),
                    onSearch = {},
                    autoFocus = true,
                )
            }
        }

        composeTestRule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun readOnlySearchBar_triggersOnlyReadOnlyClick() {
        val clickCount = mutableIntStateOf(0)
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeSearchBar(
                    state = rememberTextFieldState(),
                    onSearch = {},
                    placeholder = "Find a book",
                    readOnly = true,
                    onReadOnlyClick = { clickCount.intValue++ },
                )
            }
        }

        composeTestRule.onNodeWithText("Find a book").performClick()

        composeTestRule.runOnIdle { assertEquals(1, clickCount.intValue) }
    }

    @Test
    fun editableSearchBar_doesNotTriggerReadOnlyClick() {
        val clickCount = mutableIntStateOf(0)
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeSearchBar(
                    state = rememberTextFieldState(),
                    onSearch = {},
                    onReadOnlyClick = { clickCount.intValue++ },
                )
            }
        }

        composeTestRule.onNode(hasSetTextAction()).performClick()

        composeTestRule.runOnIdle { assertEquals(0, clickCount.intValue) }
    }

    @Test
    fun sectionHeader_invokesAction() {
        val clickCount = mutableIntStateOf(0)
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeSectionHeader(
                    title = "Recent searches",
                    actionLabel = "Clear all",
                    onActionClick = { clickCount.intValue++ },
                )
            }
        }

        composeTestRule.onNodeWithText("Clear all").performClick()

        composeTestRule.runOnIdle { assertEquals(1, clickCount.intValue) }
    }

    @Test
    fun tabRow_reportsSelectedTabIndex() {
        val selectedTabIndex = mutableIntStateOf(-1)
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeTabRow(
                    tabs = listOf("Book information", "My records"),
                    selectedTabIndex = 0,
                    onTabSelected = { selectedTabIndex.intValue = it },
                )
            }
        }

        composeTestRule.onNodeWithText("My records").performClick()

        composeTestRule.runOnIdle { assertEquals(1, selectedTabIndex.intValue) }
    }
}
