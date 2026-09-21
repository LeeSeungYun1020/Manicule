package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.leeseungyun1020.manicule.core.designsystem.R
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FeedbackStateComponentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun emptyState_supportsOneAction() {
        var clickCount = 0
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeEmptyState(
                    title = "No records",
                    actions = {
                        ManiculeButton(
                            onClick = { clickCount++ },
                            text = "Add record",
                        )
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Add record").performClick()

        composeTestRule.runOnIdle { assertEquals(1, clickCount) }
    }

    @Test
    fun emptyState_supportsTwoActions() {
        var primaryClickCount = 0
        var secondaryClickCount = 0
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeEmptyState(
                    title = "Camera permission required",
                    actions = {
                        ManiculeButton(
                            onClick = { primaryClickCount++ },
                            text = "Use camera",
                        )
                        ManiculeOutlinedButton(
                            onClick = { secondaryClickCount++ },
                            text = "Search",
                        )
                    },
                )
            }
        }

        composeTestRule.onNodeWithText("Use camera").performClick()
        composeTestRule.onNodeWithText("Search").performClick()

        composeTestRule.runOnIdle {
            assertEquals(1, primaryClickCount)
            assertEquals(1, secondaryClickCount)
        }
    }

    @Test
    fun loading_usesCallerProvidedSize() {
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeLoading(
                    modifier =
                        Modifier
                            .testTag("loading")
                            .size(
                                width = ManiculeSize.coverSmallWidth,
                                height = ManiculeSize.touchTargetMin,
                            ),
                )
            }
        }

        composeTestRule
            .onNodeWithTag("loading")
            .assertWidthIsEqualTo(ManiculeSize.coverSmallWidth)
            .assertHeightIsEqualTo(ManiculeSize.touchTargetMin)
    }

    @Test
    fun undoSnackbar_replacesCurrentSnackbarWithLatestRequest() {
        lateinit var hostState: SnackbarHostState
        lateinit var coroutineScope: CoroutineScope
        var firstResult: SnackbarResult? = null
        var secondResult: SnackbarResult? = null
        composeTestRule.setContent {
            ManiculeTheme {
                hostState = remember { SnackbarHostState() }
                coroutineScope = rememberCoroutineScope()
                ManiculeSnackbarHost(hostState = hostState)
            }
        }

        composeTestRule.runOnIdle {
            coroutineScope.launch {
                firstResult = hostState.showUndoSnackbar("First message", "Undo")
            }
        }
        composeTestRule.onNodeWithText("First message").assertIsDisplayed()

        composeTestRule.runOnIdle {
            coroutineScope.launch {
                secondResult = hostState.showUndoSnackbar("Second message", "Undo")
            }
        }
        composeTestRule.waitUntil {
            composeTestRule.onAllNodesWithText("Second message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Second message").assertIsDisplayed()
        composeTestRule.runOnIdle { assertEquals(SnackbarResult.Dismissed, firstResult) }

        composeTestRule.onNodeWithText("Undo").performClick()
        composeTestRule.runOnIdle { assertEquals(SnackbarResult.ActionPerformed, secondResult) }
    }

    @Test
    fun errorState_withRetry_displaysContentAndCallsRetry() {
        var retryCount = 0
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeErrorState(
                    title = "Failed to load",
                    icon = ManiculeIcons.NetworkError,
                    description = "Check your connection",
                    onRetry = { retryCount++ },
                    retryText = "Retry now",
                )
            }
        }

        composeTestRule.onNodeWithText("Failed to load").assertIsDisplayed()
        composeTestRule.onNodeWithText("Check your connection").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry now").assertIsDisplayed().performClick()
        composeTestRule.runOnIdle { assertEquals(1, retryCount) }
    }

    @Test
    fun errorState_withoutRetry_displaysOnlyTitleAndDescription() {
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeErrorState(
                    title = "Connection error",
                    icon = ManiculeIcons.NetworkError,
                    description = "Please try again later",
                )
            }
        }

        composeTestRule.onNodeWithText("Connection error").assertIsDisplayed()
        composeTestRule.onNodeWithText("Please try again later").assertIsDisplayed()
        assertEquals(0, composeTestRule.onAllNodesWithText(context.getString(R.string.core_designsystem_retry)).fetchSemanticsNodes().size)
    }

    @Test
    fun networkErrorState_withRetry_displaysDefaultTextAndCallsRetry() {
        var retryCount = 0
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeNetworkErrorState(
                    onRetry = { retryCount++ },
                )
            }
        }

        composeTestRule.onNodeWithText(context.getString(R.string.core_designsystem_network_error_title)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.core_designsystem_network_error_description)).assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.core_designsystem_retry)).assertIsDisplayed().performClick()
        composeTestRule.runOnIdle { assertEquals(1, retryCount) }
    }
}
