package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.feature.settings.components.NotificationPermissionRationale
import com.leeseungyun1020.manicule.feature.settings.components.ReminderUiStatePreviewProvider
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun themeOptions_showSavedSelectionAndSendChoice() {
        var selected: ThemeMode? = null
        composeRule.setSettingsContent(
            state = SettingsUiState(
                reminder = ReminderUiState.Content(ReminderConfig.Default),
                theme = ThemeUiState.Content(ThemeMode.SYSTEM),
            ),
            onThemeSelected = { selected = it },
        )
        composeRule.onNodeWithText(context.getString(R.string.settings_theme_system)).assertIsSelected()
        composeRule.onNodeWithText(context.getString(R.string.settings_theme_dark)).performClick()
        assertThat(selected).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun themeError_showsPreviousButBlocksSelectionUntilRetry() {
        var retried = false
        composeRule.setSettingsContent(
            state = SettingsUiState(
                reminder = ReminderUiState.Content(ReminderConfig.Default),
                theme = ThemeUiState.Error(ThemeMode.LIGHT),
            ),
            onRetryPreferences = { retried = true },
        )
        composeRule.onNodeWithText(context.getString(R.string.settings_theme_light)).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.settings_theme_previous, context.getString(R.string.settings_theme_light)))
            .assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_retry)).performClick()
        assertThat(retried).isTrue()
    }

    @Test
    fun permissionRationale_cancelDoesNotRequestPermission() {
        var requested = false
        var dismissed = false
        composeRule.setContent {
            ManiculeTheme {
                NotificationPermissionRationale(
                    onContinue = { requested = true },
                    onDismiss = { dismissed = true },
                )
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_cancel)).performClick()

        assertThat(dismissed).isTrue()
        assertThat(requested).isFalse()
    }

    @Test
    fun permissionRationale_continueRequestsPermission() {
        var requested = false
        composeRule.setContent {
            ManiculeTheme {
                NotificationPermissionRationale(onContinue = { requested = true }, onDismiss = {})
            }
        }

        composeRule.onNodeWithText(context.getString(R.string.settings_notification_permission_denied)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_continue)).performClick()

        assertThat(requested).isTrue()
    }

    @Test
    fun disabledReminder_hidesTimeAndRequestsEnable() {
        var enabled: Boolean? = null
        composeRule.setSettingsContent(
            state = SettingsUiState(ReminderUiState.Content(ReminderConfig.Default)),
            onReminderEnabledChange = { enabled = it },
        )

        composeRule.onNode(isToggleable()).assertIsOff().performClick()

        assertThat(enabled).isTrue()
        composeRule.onNodeWithText(context.getString(R.string.settings_reminder_time)).assertDoesNotExist()
    }

    @Test
    fun enabledReminder_showsTimeAndRequestsDisable() {
        var enabled: Boolean? = null
        composeRule.setSettingsContent(
            state =
                SettingsUiState(
                    ReminderUiState.Content(
                        ReminderConfig(enabled = true, time = LocalTime(21, 0)),
                    ),
                ),
            onReminderEnabledChange = { enabled = it },
        )

        composeRule.onNode(isToggleable()).assertIsOn().performClick()

        assertThat(enabled).isFalse()
        composeRule.onNodeWithText(context.getString(R.string.settings_reminder_time)).assertIsDisplayed()
    }

    @Test
    fun timePicker_cancelKeepsValue_andConfirmReturnsInitialTime() {
        var selectedTime: LocalTime? = null
        composeRule.setSettingsContent(
            state =
                SettingsUiState(
                    ReminderUiState.Content(
                        ReminderConfig(enabled = true, time = LocalTime(8, 30)),
                    ),
                ),
            onReminderTimeChange = { selectedTime = it },
        )

        composeRule.onNodeWithText(context.getString(R.string.settings_reminder_time)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_cancel)).performClick()
        assertThat(selectedTime).isNull()

        composeRule.onNodeWithText(context.getString(R.string.settings_reminder_time)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.settings_confirm)).performClick()

        assertThat(selectedTime).isEqualTo(LocalTime(8, 30))
    }

    @Test
    fun updatingState_disablesControls() {
        composeRule.setSettingsContent(
            state =
                SettingsUiState(
                    ReminderUiState.Content(
                        reminder = ReminderConfig(enabled = true, time = LocalTime(21, 0)),
                        isUpdating = true,
                    ),
                ),
        )

        composeRule.onNode(isToggleable()).assertIsNotEnabled()
    }

    @Test
    fun errorState_retriesLoadingPreferences() {
        var retried = false
        composeRule.setSettingsContent(
            state = SettingsUiState(ReminderUiState.Error()),
            onRetryPreferences = { retried = true },
        )

        composeRule.onNodeWithText(context.getString(R.string.settings_retry)).performClick()

        assertThat(retried).isTrue()
    }

    @Test
    fun loadingState_isAccessible() {
        composeRule.setSettingsContent(state = SettingsUiState(ReminderUiState.Loading()))

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.settings_loading))
            .assertIsDisplayed()
    }

    @Test
    fun compactContent_usesAvailableWidth() {
        composeRule.setSettingsContent(
            state = SettingsUiState(ReminderUiState.Content(ReminderConfig.Default)),
            width = 320,
        )
        composeRule.onNodeWithTag(SETTINGS_CONTENT_TEST_TAG).assertWidthIsEqualTo(320.dp)
    }

    @Test
    fun expandedContent_isLimitedToMaximumWidth() {
        composeRule.setSettingsContent(
            state = SettingsUiState(ReminderUiState.Content(ReminderConfig.Default)),
            width = 840,
        )
        composeRule.onNodeWithTag(SETTINGS_CONTENT_TEST_TAG).assertWidthIsEqualTo(ManiculeSize.contentMaxWidth)
        composeRule
            .onNode(hasText(context.getString(R.string.settings_reading_reminder)) and isToggleable())
            .assertIsDisplayed()
    }

    @Test
    fun everyReminderState_keepsScreenAndSectionVisible() {
        val state = mutableStateOf(SettingsUiState())
        val darkTheme = mutableStateOf(false)
        composeRule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1.5f)) {
                ManiculeTheme(darkTheme = darkTheme.value) {
                    SettingsScreen(
                        uiState = state.value,
                        snackbarHostState = remember { SnackbarHostState() },
                        onReminderEnabledChange = {},
                        onReminderTimeChange = {},
                        onRetryPreferences = {},
                        onThemeSelected = {},
                        onNavigateToLicenses = {},
                    )
                }
            }
        }
        listOf(false, true).forEach { dark ->
            composeRule.runOnIdle { darkTheme.value = dark }
            ReminderUiStatePreviewProvider().values.forEach { reminder ->
                composeRule.runOnIdle { state.value = SettingsUiState(reminder) }
                composeRule.onNodeWithTag(SETTINGS_CONTENT_TEST_TAG).assertIsDisplayed()
                composeRule.onNodeWithText(context.getString(R.string.settings_title)).assertIsDisplayed()
                composeRule.onNodeWithText(context.getString(R.string.settings_notifications_section)).assertIsDisplayed()
                composeRule.onNodeWithText(context.getString(R.string.settings_reading_reminder)).assertIsDisplayed()
            }
        }
    }

    @Test
    fun firstLoadFailure_doesNotShowDefaultOffSwitch() {
        composeRule.setSettingsContent(SettingsUiState(ReminderUiState.Error()))
        composeRule.onNode(isToggleable()).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.settings_reminder_time)).assertDoesNotExist()
        composeRule.onNodeWithText(context.getString(R.string.settings_load_error_title)).assertIsDisplayed()
    }

    @Test
    fun loadingWithoutPreviousValue_doesNotShowSwitch() {
        composeRule.setSettingsContent(SettingsUiState())
        composeRule.onNode(isToggleable()).assertDoesNotExist()
    }

    @Test
    fun readFailure_keepsPreviousValueDisablesControlsAndClosesPicker() {
        val previous = ReminderConfig(true, LocalTime(8, 30))
        val state = mutableStateOf(SettingsUiState(ReminderUiState.Content(previous)))
        var changes = 0
        composeRule.setContent {
            ManiculeTheme {
                SettingsScreen(
                    uiState = state.value,
                    snackbarHostState = remember { SnackbarHostState() },
                    onReminderEnabledChange = { changes++ },
                    onReminderTimeChange = { changes++ },
                    onRetryPreferences = {},
                    onThemeSelected = {},
                    onNavigateToLicenses = {},
                )
            }
        }
        composeRule.onNodeWithText(context.getString(R.string.settings_reminder_time)).performClick()
        composeRule.onNode(isDialog()).assertExists()
        composeRule.runOnIdle { state.value = SettingsUiState(ReminderUiState.Error(previous)) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
        composeRule.onNode(isToggleable()).assertIsOn().assertIsNotEnabled()
        composeRule.onNodeWithText(context.getString(R.string.settings_reminder_time)).assertIsNotEnabled()
        composeRule.onNodeWithText(context.getString(R.string.settings_previous_reminder_description)).assertIsDisplayed()
        assertThat(changes).isEqualTo(0)

        composeRule.runOnIdle { state.value = SettingsUiState(ReminderUiState.Loading(previous)) }
        composeRule.onNode(isToggleable()).assertIsOn().assertIsNotEnabled()
        composeRule.onNodeWithText(context.getString(R.string.settings_retry)).assertDoesNotExist()
        composeRule.runOnIdle { state.value = SettingsUiState(ReminderUiState.Content(previous)) }
        composeRule.onNode(isDialog()).assertDoesNotExist()
    }

    @Test
    fun retryTransition_keepsScrollPosition() {
        val previous = ReminderConfig(true, LocalTime(8, 30))
        val state = mutableStateOf(SettingsUiState(ReminderUiState.Error(previous)))
        composeRule.setContent {
            ManiculeTheme {
                Box(Modifier.requiredHeight(200.dp)) {
                    SettingsScreen(
                        uiState = state.value,
                        snackbarHostState = remember { SnackbarHostState() },
                        onReminderEnabledChange = {},
                        onReminderTimeChange = {},
                        onRetryPreferences = {},
                        onThemeSelected = {},
                        onNavigateToLicenses = {},
                    )
                }
            }
        }
        val content = composeRule.onNodeWithTag(SETTINGS_CONTENT_TEST_TAG)
        content.performSemanticsAction(SemanticsActions.ScrollBy) { it(0f, 1000f) }
        composeRule.waitForIdle()
        val previousScroll = content.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange].value()
        assertThat(previousScroll).isGreaterThan(0f)

        composeRule.runOnIdle { state.value = SettingsUiState(ReminderUiState.Loading(previous)) }
        val loadingScroll = content.fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange]
        assertThat(loadingScroll.maxValue()).isGreaterThan(0f)
        assertThat(loadingScroll.value()).isEqualTo(minOf(previousScroll, loadingScroll.maxValue()))
    }

    @Test
    fun supportSection_displaysLicensesAndVersionInfo() {
        composeRule.setSettingsContent(
            state = SettingsUiState(ReminderUiState.Content(ReminderConfig.Default)),
            appVersion = "1.0.0",
        )

        composeRule.onNodeWithText(context.getString(R.string.settings_support_section)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_licenses)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.settings_version_info)).assertIsDisplayed()
        composeRule.onNodeWithText("1.0.0").assertIsDisplayed()
    }

    @Test
    fun supportSection_clickLicenses_triggersCallback() {
        var clicked = false
        composeRule.setSettingsContent(
            state = SettingsUiState(ReminderUiState.Content(ReminderConfig.Default)),
            onNavigateToLicenses = { clicked = true },
        )

        composeRule.onNodeWithText(context.getString(R.string.settings_licenses)).performClick()
        assertThat(clicked).isTrue()
    }

    private companion object {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
    }
}

private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setSettingsContent(
    state: SettingsUiState,
    onReminderEnabledChange: (Boolean) -> Unit = {},
    onReminderTimeChange: (LocalTime) -> Unit = {},
    onRetryPreferences: () -> Unit = {},
    onThemeSelected: (ThemeMode) -> Unit = {},
    appVersion: String = "",
    onNavigateToLicenses: () -> Unit = {},
    width: Int? = null,
) {
    setContent {
        ManiculeTheme {
            Box(modifier = width?.let { Modifier.requiredWidth(it.dp) } ?: Modifier) {
                SettingsScreen(
                    uiState = state,
                    snackbarHostState = remember { SnackbarHostState() },
                    onReminderEnabledChange = onReminderEnabledChange,
                    onReminderTimeChange = onReminderTimeChange,
                    onRetryPreferences = onRetryPreferences,
                    onThemeSelected = onThemeSelected,
                    onNavigateToLicenses = onNavigateToLicenses,
                    appVersion = appVersion,
                )
            }
        }
    }
}
