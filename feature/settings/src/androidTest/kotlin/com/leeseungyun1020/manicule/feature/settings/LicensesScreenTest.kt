package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
                licenseUrl = "https://example.com/license-a",
                url = "https://example.com/test",
            ),
        )

    @Test
    fun licensesScreen_displaysTopBarTitle_andNavigatesBack() {
        var backPressed = false
        composeRule.setContent {
            ManiculeTheme {
                LicensesScreen(
                    uiState = LicensesUiState.Loading,
                    onNavigateBack = { backPressed = true },
                    onRetry = {},
                    onOpenUrl = {},
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
                        ),
                    onNavigateBack = {},
                    onRetry = {},
                    onOpenUrl = {},
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
    fun licensesScreen_viewFullText_opensLibraryLicenseUrl() {
        val customLicenseUrl = "https://example.com/custom-license.txt"
        val customLibrary =
            OpenSourceLibrary(
                name = "Custom Library",
                copyright = "Copyright 2026 Custom Authors",
                license = "GPLv2 with Classpath Exception",
                licenseUrl = customLicenseUrl,
            )
        var openedUrl: String? = null
        composeRule.setContent {
            ManiculeTheme {
                LicensesScreen(
                    uiState =
                        LicensesUiState.Success(
                            libraries = listOf(customLibrary),
                        ),
                    onNavigateBack = {},
                    onRetry = {},
                    onOpenUrl = { openedUrl = it },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_view_full_text)).performClick()

        assertThat(openedUrl).isEqualTo(customLicenseUrl)
    }

    @Test
    fun licensesScreen_libraryWithUrl_showsDetailsButton_andTriggersCallback() {
        var openedUrl: String? = null
        composeRule.setContent {
            ManiculeTheme {
                LicensesScreen(
                    uiState =
                        LicensesUiState.Success(
                            libraries = testLibraries,
                        ),
                    onNavigateBack = {},
                    onRetry = {},
                    onOpenUrl = { openedUrl = it },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_view_details)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_view_details)).performClick()

        assertThat(openedUrl).isEqualTo("https://example.com/test")
    }

    @Test
    fun licensesScreen_libraryWithoutUrl_doesNotShowDetailsButton() {
        val librariesWithoutUrl =
            listOf(
                OpenSourceLibrary(
                    name = "No Url Library",
                    copyright = "Copyright 2026 Test Authors",
                    license = "Apache License 2.0",
                    licenseUrl = "https://example.com/license",
                    url = null,
                ),
            )
        composeRule.setContent {
            ManiculeTheme {
                LicensesScreen(
                    uiState =
                        LicensesUiState.Success(
                            libraries = librariesWithoutUrl,
                        ),
                    onNavigateBack = {},
                    onRetry = {},
                    onOpenUrl = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_view_details)).assertDoesNotExist()
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
                    onOpenUrl = {},
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses_load_error)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(DesignSystemR.string.core_designsystem_retry))
            .performClick()

        assertThat(retried).isTrue()
    }

    @Test
    fun licensesScreen_displaysSnackbarMessage() {
        val snackbarHostState = SnackbarHostState()
        var coroutineScope: CoroutineScope? = null
        composeRule.setContent {
            coroutineScope = rememberCoroutineScope()
            ManiculeTheme {
                LicensesScreen(
                    uiState = LicensesUiState.Loading,
                    onNavigateBack = {},
                    onRetry = {},
                    onOpenUrl = {},
                    snackbarHostState = snackbarHostState,
                )
            }
        }

        coroutineScope?.launch {
            snackbarHostState.showSnackbar("Test error message")
        }

        composeRule.onNodeWithText("Test error message").assertIsDisplayed()
    }

    private companion object {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
    }
}
