package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.feature.settings.components.NotificationPermissionRationale
import kotlinx.datetime.LocalTime
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

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
            state = SettingsUiState.Content(ReminderConfig.Default),
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
                SettingsUiState.Content(
                    ReminderConfig(enabled = true, time = LocalTime(21, 0)),
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
                SettingsUiState.Content(
                    ReminderConfig(enabled = true, time = LocalTime(8, 30)),
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
                SettingsUiState.Content(
                    reminder = ReminderConfig(enabled = true, time = LocalTime(21, 0)),
                    isUpdating = true,
                ),
        )

        composeRule.onNode(isToggleable()).assertIsNotEnabled()
    }

    @Test
    fun errorState_retriesLoadingPreferences() {
        var retried = false
        composeRule.setSettingsContent(
            state = SettingsUiState.Error,
            onRetryPreferences = { retried = true },
        )

        composeRule.onNodeWithText(context.getString(R.string.settings_retry)).performClick()

        assertThat(retried).isTrue()
    }

    @Test
    fun loadingState_isAccessible() {
        composeRule.setSettingsContent(state = SettingsUiState.Loading)

        composeRule
            .onNodeWithContentDescription(context.getString(R.string.settings_loading))
            .assertIsDisplayed()
    }

    @Test
    fun compactContent_usesAvailableWidth() {
        composeRule.setSettingsContent(
            state = SettingsUiState.Content(ReminderConfig.Default),
            width = 320,
        )
        composeRule.onNodeWithTag(SETTINGS_CONTENT_TEST_TAG).assertWidthIsEqualTo(320.dp)
    }

    @Test
    fun expandedContent_isLimitedToMaximumWidth() {
        composeRule.setSettingsContent(
            state = SettingsUiState.Content(ReminderConfig.Default),
            width = 840,
        )
        composeRule.onNodeWithTag(SETTINGS_CONTENT_TEST_TAG).assertWidthIsEqualTo(ManiculeSize.contentMaxWidth)
        composeRule
            .onNode(hasText(context.getString(R.string.settings_reading_reminder)) and isToggleable())
            .assertIsDisplayed()
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
                )
            }
        }
    }
}
