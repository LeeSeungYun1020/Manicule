package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.leeseungyun1020.manicule.core.designsystem.R as DesignSystemR

@RunWith(AndroidJUnit4::class)
class LicensesScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val testLibraries =
        listOf(
            OpenSourceLibrary(
                name = "Test Library A",
                copyright = "Copyright 2026 Test Authors",
                license = "Apache License 2.0",
                url = "https://example.com/test",
            ),
        )
    private val testLicenseText = "Terms and Conditions of Apache License 2.0"

    @Test
    fun licensesScreen_displaysTopBarTitle_andNavigatesBack() {
        var backPressed = false
        composeRule.setContent {
            ManiculeTheme {
                LicensesScreen(
                    uiState = LicensesUiState.Loading,
                    onNavigateBack = { backPressed = true },
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_title)).assertIsDisplayed()
        composeRule.onNodeWithContentDescription(context.getString(DesignSystemR.string.core_designsystem_back))
            .performClick()

        assertThat(backPressed).isTrue()
    }

    @Test
    fun licensesScreen_displaysLibrariesList() {
        composeRule.setContent {
            ManiculeTheme {
                LicensesScreen(
                    uiState =
                        LicensesUiState.Success(
                            libraries = testLibraries,
                            licenseText = testLicenseText,
                        ),
                    onNavigateBack = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_description)).assertIsDisplayed()
        composeRule.onNodeWithText("Test Library A").assertIsDisplayed()
        composeRule.onNodeWithText("Copyright 2026 Test Authors").assertIsDisplayed()
        composeRule.onNodeWithText("Apache License 2.0").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_view_full_text)).assertIsDisplayed()
    }

    @Test
    fun licensesScreen_viewFullText_opensAndClosesDialog() {
        composeRule.setContent {
            ManiculeTheme {
                LicensesScreen(
                    uiState =
                        LicensesUiState.Success(
                            libraries = testLibraries,
                            licenseText = testLicenseText,
                        ),
                    onNavigateBack = {},
                    onRetry = {},
                )
            }
        }

        composeRule.onNodeWithText(testLicenseText).assertDoesNotExist()

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_view_full_text)).performClick()

        composeRule.onNodeWithText(testLicenseText).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_close)).performClick()

        composeRule.onNodeWithText(testLicenseText).assertDoesNotExist()
    }

    @Test
    fun licensesScreen_errorState_showsErrorAndRetries() {
        var retried = false
        composeRule.setContent {
            ManiculeTheme {
                LicensesScreen(
                    uiState = LicensesUiState.Error,
                    onNavigateBack = {},
                    onRetry = { retried = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_load_error)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(DesignSystemR.string.core_designsystem_retry))
            .performClick()

        assertThat(retried).isTrue()
    }

    private companion object {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
    }
}
