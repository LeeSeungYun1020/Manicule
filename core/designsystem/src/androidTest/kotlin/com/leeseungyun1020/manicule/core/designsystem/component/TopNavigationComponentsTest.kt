package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.leeseungyun1020.manicule.core.designsystem.R
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
    fun searchBar_requestsInitialFocus() {
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeSearchBar(
                    state = rememberTextFieldState(),
                    onSearch = {},
                    requestInitialFocus = true,
                )
            }
        }

        composeTestRule.onNode(hasSetTextAction()).assertIsFocused()
    }

    @Test
    fun searchEntry_invokesClick() {
        val clickCount = mutableIntStateOf(0)
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeSearchEntry(
                    onClick = { clickCount.intValue++ },
                    placeholder = "Find a book",
                )
            }
        }

        val searchEntry = composeTestRule.onNodeWithContentDescription("Find a book")
        searchEntry
            .assertHasClickAction()
            .assert(
                SemanticsMatcher.expectValue(
                    SemanticsProperties.Role,
                    Role.Button,
                ),
            )
        composeTestRule.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        searchEntry.performClick()

        composeTestRule.runOnIdle { assertEquals(1, clickCount.intValue) }
    }

    @Test
    fun sectionHeader_invokesAction() {
        val clickCount = mutableIntStateOf(0)
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeSectionHeader(
                    title = "Recent searches",
                    action =
                        ManiculeSectionHeaderAction(
                            label = "Clear all",
                            onClick = { clickCount.intValue++ },
                        ),
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

    @Test
    fun tabRow_withEmptyTabs_rendersNothing() {
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeTabRow(
                    tabs = emptyList<String>(),
                    selectedTabIndex = 0,
                    onTabSelected = {},
                )
            }
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun tabRow_withInvalidSelectedIndex_rendersNothing() {
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeTabRow(
                    tabs = listOf("Book information", "My records"),
                    selectedTabIndex = 2,
                    onTabSelected = {},
                )
            }
        }

        composeTestRule.onAllNodes(hasText("Book information")).assertCountEquals(0)
        composeTestRule.onAllNodes(hasText("My records")).assertCountEquals(0)
    }

    @Test
    fun topAppBar_themeTransition_updatesBackgroundColorImmediatelyWithoutAnimationFlicker() {
        var darkTheme by mutableStateOf(false)
        composeTestRule.mainClock.autoAdvance = false

        composeTestRule.setContent {
            ManiculeTheme(darkTheme = darkTheme) {
                ManiculeTopAppBar(
                    title = "Title",
                    modifier = Modifier.testTag("appBar"),
                )
            }
        }

        // Render initial frame (Light theme)
        composeTestRule.mainClock.advanceTimeByFrame()
        val lightSurfaceColor = Color(0xFFFFFCF7).toArgb()
        val darkSurfaceColor = Color(0xFF17130C).toArgb()

        val initialPixel =
            composeTestRule
                .onNodeWithTag("appBar")
                .captureToImage()
                .toPixelMap()[10, 10]
                .toArgb()
        assertEquals(lightSurfaceColor, initialPixel)

        // Switch Light -> Dark
        darkTheme = true
        composeTestRule.mainClock.advanceTimeByFrame()

        val switchedToDarkPixel =
            composeTestRule
                .onNodeWithTag("appBar")
                .captureToImage()
                .toPixelMap()[10, 10]
                .toArgb()
        assertEquals(darkSurfaceColor, switchedToDarkPixel)

        // Switch Dark -> Light
        darkTheme = false
        composeTestRule.mainClock.advanceTimeByFrame()

        val switchedToLightPixel =
            composeTestRule
                .onNodeWithTag("appBar")
                .captureToImage()
                .toPixelMap()[10, 10]
                .toArgb()
        assertEquals(lightSurfaceColor, switchedToLightPixel)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun topAppBar_retainsScrollBehaviorStateAndDisplaysContentAcrossThemeTransition() {
        var darkTheme by mutableStateOf(false)
        lateinit var scrollBehavior: TopAppBarScrollBehavior

        composeTestRule.setContent {
            val behavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
            scrollBehavior = behavior
            ManiculeTheme(darkTheme = darkTheme) {
                ManiculeTopAppBar(
                    title = "Book Detail",
                    onNavigateBack = {},
                    scrollBehavior = behavior,
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More actions",
                            )
                        }
                    },
                )
            }
        }

        // Set an offset in the scroll behavior state
        composeTestRule.runOnIdle {
            scrollBehavior.state.heightOffset = -24f
            scrollBehavior.state.contentOffset = -48f
        }

        // Verify initial elements are displayed
        composeTestRule.onNodeWithText("Book Detail").assertIsDisplayed()
        val backDesc =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .getString(R.string.core_designsystem_back)
        composeTestRule.onNodeWithContentDescription(backDesc).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("More actions").assertIsDisplayed()

        // Switch theme: Light -> Dark
        darkTheme = true
        composeTestRule.waitForIdle()

        // Verify title and icons are still displayed after theme change
        composeTestRule.onNodeWithText("Book Detail").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(backDesc).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("More actions").assertIsDisplayed()

        // Verify scroll behavior state was preserved across theme change
        assertEquals(-24f, scrollBehavior.state.heightOffset, 0.001f)
        assertEquals(-48f, scrollBehavior.state.contentOffset, 0.001f)

        // Switch theme: Dark -> Light
        darkTheme = false
        composeTestRule.waitForIdle()

        // Verify title, icons, and scroll behavior state remain preserved
        composeTestRule.onNodeWithText("Book Detail").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(backDesc).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("More actions").assertIsDisplayed()
        assertEquals(-24f, scrollBehavior.state.heightOffset, 0.001f)
        assertEquals(-48f, scrollBehavior.state.contentOffset, 0.001f)
    }
}
