package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManiculeStatTileTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun valueAndLabel_areMergedIntoOneSemanticsNode() {
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeStatTile(
                    value = "12 days",
                    label = "Streak",
                )
            }
        }

        composeTestRule
            .onNode(hasText("12 days") and hasText("Streak"))
            .assertIsDisplayed()
    }

    @Test
    fun optionalIcon_isDisplayed() {
        composeTestRule.setContent {
            ManiculeTheme {
                ManiculeStatTile(
                    value = "1,248p",
                    label = "Pages read",
                    icon = {
                        Box(
                            modifier =
                                Modifier
                                    .testTag("stat-icon")
                                    .size(ManiculeSize.iconSm),
                        )
                    },
                )
            }
        }

        composeTestRule.onNodeWithTag("stat-icon", useUnmergedTree = true).assertIsDisplayed()
    }
}
