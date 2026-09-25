package com.leeseungyun1020.manicule.navigation

import android.os.Build
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.core.view.WindowCompat
import com.leeseungyun1020.manicule.APP_THEME_SURFACE_TAG
import com.leeseungyun1020.manicule.DARK_NAVIGATION_BAR_SCRIM
import com.leeseungyun1020.manicule.LIGHT_NAVIGATION_BAR_SCRIM
import com.leeseungyun1020.manicule.MainActivity
import com.leeseungyun1020.manicule.R
import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.core.notifications.ReminderNotificationPublisher
import com.leeseungyun1020.manicule.core.notifications.di.NotificationsModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalTime
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NotificationsModule::class)
class ThemeNavigationTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var preferences: UserPreferencesRepository

    @BindValue
    @JvmField
    val reminderScheduler: ReminderScheduler = object : ReminderScheduler {
        override suspend fun schedule(time: LocalTime) = Unit

        override suspend fun scheduleNext(time: LocalTime) = Unit

        override suspend fun cancel() = Unit
    }

    @BindValue
    @JvmField
    val notificationPublisher: ReminderNotificationPublisher = object : ReminderNotificationPublisher {
        override fun publish(books: List<ReminderContent.Book>) = Unit
    }

    @Test
    fun selectingTheme_recolorsAnotherTabWithoutRestart() {
        hilt.inject()
        val settingsTab = compose.activity.getString(R.string.tab_settings)
        val libraryTab = compose.activity.getString(R.string.tab_library)
        val lightOption = compose.activity.getString(com.leeseungyun1020.manicule.feature.settings.R.string.settings_theme_light)
        val darkOption = compose.activity.getString(com.leeseungyun1020.manicule.feature.settings.R.string.settings_theme_dark)
        try {
            compose.onNodeWithText(settingsTab).performClick()
            compose.onNodeWithText(lightOption).performClick()
            awaitMode(ThemeMode.LIGHT)
            awaitSystemBars(lightIcons = true)
            compose.onNodeWithText(libraryTab).performClick()
            compose.waitForIdle()
            val light = paletteSamples()

            compose.onNodeWithText(settingsTab).performClick()
            compose.onNodeWithText(darkOption).performClick()
            awaitMode(ThemeMode.DARK)
            awaitSystemBars(lightIcons = false)
            compose.onNodeWithText(libraryTab).performClick()
            compose.waitForIdle()
            val dark = paletteSamples()

            assertTrue(light.zip(dark).count { (before, after) -> before != after } >= 12)
        } finally {
            runBlocking { preferences.setThemeMode(ThemeMode.SYSTEM) }
        }
    }

    private fun awaitMode(mode: ThemeMode) {
        compose.waitUntil(5_000) {
            runBlocking { preferences.userPreferences.first().themeMode == mode }
        }
    }

    private fun awaitSystemBars(lightIcons: Boolean) {
        val expectedNavigationBarColor = if (lightIcons) LIGHT_NAVIGATION_BAR_SCRIM else DARK_NAVIGATION_BAR_SCRIM
        compose.waitUntil(5_000) {
            WindowCompat.getInsetsController(compose.activity.window, compose.activity.window.decorView).let { controller ->
                controller.isAppearanceLightStatusBars == lightIcons &&
                    controller.isAppearanceLightNavigationBars == lightIcons &&
                    (Build.VERSION.SDK_INT > 28 || compose.activity.window.navigationBarColor == expectedNavigationBarColor)
            }
        }
    }

    private fun paletteSamples() =
        compose.onNodeWithTag(APP_THEME_SURFACE_TAG).captureToImage().toPixelMap().let { pixels ->
            (1..5).flatMap { row ->
                (1..5).map { column -> pixels[pixels.width * column / 6, pixels.height * row / 6] }
            }
        }
}
