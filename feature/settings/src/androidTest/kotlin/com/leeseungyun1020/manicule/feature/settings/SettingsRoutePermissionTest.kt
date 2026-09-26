package com.leeseungyun1020.manicule.feature.settings

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.app.ActivityOptionsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.domain.settings.GetUserPreferencesUseCase
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import com.leeseungyun1020.manicule.core.domain.settings.SetReminderUseCase
import com.leeseungyun1020.manicule.core.domain.settings.SetThemeUseCase
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 33)
class SettingsRoutePermissionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private var granted = false
    private var rationale = false
    private val settingsIntents = mutableListOf<Intent>()
    private var notificationSettingsUnavailable = false
    private val registry = PermissionRegistry()
    private val repository = RecordingPreferences()
    private val scheduler = RecordingScheduler()

    @Before
    fun setUp() {
        val permissionContext = object : ContextWrapper(context) {
            override fun startActivity(intent: Intent) {
                settingsIntents += intent
                if (notificationSettingsUnavailable && intent.action == Settings.ACTION_APP_NOTIFICATION_SETTINGS) {
                    throw ActivityNotFoundException()
                }
            }

            override fun checkSelfPermission(permission: String): Int =
                if (granted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
        }
        val permissionActivity = composeRule.runOnUiThread {
            object : Activity() {
                override fun shouldShowRequestPermissionRationale(permission: String): Boolean = rationale
            }
        }
        val registryOwner = object : ActivityResultRegistryOwner {
            override val activityResultRegistry: ActivityResultRegistry = registry
        }
        val viewModel = SettingsViewModel(
            GetUserPreferencesUseCase(repository),
            SetReminderUseCase(repository, scheduler),
            SetThemeUseCase(repository),
        )
        composeRule.setContent {
            CompositionLocalProvider(
                LocalContext provides permissionContext,
                LocalActivity provides permissionActivity,
                LocalActivityResultRegistryOwner provides registryOwner,
            ) {
                ManiculeTheme { SettingsRoute(onNavigateToLicenses = {}, viewModel = viewModel) }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun dismissedPrompt_keepsOffAndAllowsAnotherRequest() {
        repeat(3) { attempt ->
            toggleOn()
            assertThat(registry.requests).isEqualTo(attempt + 1)
            respond(false)
            assertNoUpdate()
            assertThat(settingsIntents).isEmpty()
        }
    }

    @Test
    fun deniedPrompt_nextAttemptExplainsAndCancelDoesNotRequest() {
        toggleOn()
        rationale = true
        respond(false)
        toggleOn()
        composeRule.onNodeWithText(context.getString(R.string.settings_cancel)).performClick()

        assertThat(registry.requests).isEqualTo(1)
        assertNoUpdate()
    }

    @Test
    fun explanationContinue_requestsAgainAndGrantEnablesReminder() {
        rationale = true
        toggleOn()
        assertThat(registry.requests).isEqualTo(0)
        composeRule.onNodeWithText(context.getString(R.string.settings_continue)).performClick()
        assertThat(registry.requests).isEqualTo(1)

        granted = true
        respond(true)
        composeRule.onNode(isToggleable()).assertIsOn()
        assertThat(repository.updates).isEqualTo(1)
        assertThat(scheduler.schedules).isEqualTo(1)
    }

    @Test
    fun deniedRequest_settingsRecoveryOnlyOpensAfterUserSelection() {
        toggleOn()
        respond(false)
        assertNoUpdate()
        assertThat(settingsIntents).isEmpty()

        openSettings()

        val intent = settingsIntents.single()
        assertThat(intent.action).isEqualTo(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        assertThat(intent.getStringExtra(Settings.EXTRA_APP_PACKAGE)).isEqualTo(context.packageName)
        assertNoUpdate()
        toggleOn()
        assertThat(registry.requests).isEqualTo(2)
    }

    @Test
    fun unavailableNotificationSettings_fallsBackToAppDetails() {
        notificationSettingsUnavailable = true
        toggleOn()
        respond(false)
        openSettings()

        assertThat(settingsIntents.map { it.action }).containsExactly(
            Settings.ACTION_APP_NOTIFICATION_SETTINGS,
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        ).inOrder()
        assertThat(settingsIntents.last().data.toString()).isEqualTo("package:${context.packageName}")
        assertNoUpdate()
    }

    @Test
    fun grantedInSettings_nextToggleUsesCurrentPermission() {
        toggleOn()
        respond(false)
        openSettings()
        granted = true
        toggleOn()

        composeRule.onNode(isToggleable()).assertIsOn()
        assertThat(registry.requests).isEqualTo(1)
        assertThat(repository.updates).isEqualTo(1)
        assertThat(scheduler.schedules).isEqualTo(1)
    }

    @Test
    fun grantCallbackWithRevokedPermission_doesNotEnableReminder() {
        toggleOn()
        respond(true)
        assertNoUpdate()
    }

    private fun openSettings() {
        composeRule.onNodeWithText(context.getString(R.string.settings_open_system_settings)).performClick()
        composeRule.waitForIdle()
    }

    private fun toggleOn() {
        composeRule.onNode(isToggleable()).performClick()
        composeRule.waitForIdle()
    }

    private fun respond(result: Boolean) {
        composeRule.runOnIdle { registry.respond(result) }
        composeRule.waitForIdle()
    }

    private fun assertNoUpdate() {
        composeRule.onNode(isToggleable()).assertIsOff()
        assertThat(repository.updates).isEqualTo(0)
        assertThat(scheduler.schedules).isEqualTo(0)
        assertThat(scheduler.cancellations).isEqualTo(0)
    }
}

private class PermissionRegistry : ActivityResultRegistry() {
    var requests = 0
    private var requestCode = 0

    override fun <I, O> onLaunch(
        requestCode: Int,
        contract: ActivityResultContract<I, O>,
        input: I,
        options: ActivityOptionsCompat?,
    ) {
        this.requestCode = requestCode
        requests++
    }

    fun respond(granted: Boolean) {
        dispatchResult(requestCode, granted)
    }
}

private class RecordingPreferences : UserPreferencesRepository {
    override val userPreferences = MutableStateFlow(UserPreferences.Default)
    var updates = 0

    override suspend fun setThemeMode(themeMode: ThemeMode) = Unit

    override suspend fun setReminderConfig(config: ReminderConfig) {
        updates++
        userPreferences.value = userPreferences.value.copy(reminder = config)
    }
}

private class RecordingScheduler : ReminderScheduler {
    var schedules = 0
    var cancellations = 0

    override suspend fun schedule(time: LocalTime) {
        schedules++
    }

    override suspend fun scheduleNext(time: LocalTime) = Unit

    override suspend fun cancel() {
        cancellations++
    }
}
