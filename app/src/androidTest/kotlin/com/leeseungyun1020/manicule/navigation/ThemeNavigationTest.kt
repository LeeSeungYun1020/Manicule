package com.leeseungyun1020.manicule.navigation

import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.work.Configuration
import androidx.work.WorkManager
import com.leeseungyun1020.manicule.MainActivity
import com.leeseungyun1020.manicule.core.data.repository.UserPreferencesRepository
import com.leeseungyun1020.manicule.core.model.ThemeMode
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotEquals
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class ThemeNavigationTest {
    @get:Rule(order = 0)
    val hilt = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val compose = createAndroidComposeRule<MainActivity>()

    @Inject lateinit var preferences: UserPreferencesRepository

    @Test
    fun selectingTheme_recolorsAnotherTabWithoutRestart() {
        hilt.inject()
        WorkManager.initialize(compose.activity.applicationContext, Configuration.Builder().build())
        try {
            compose.onNodeWithText("Settings").performClick()
            compose.onNodeWithText("Light").performClick()
            awaitMode(ThemeMode.LIGHT)
            compose.onNodeWithText("Library").performClick()
            compose.waitForIdle()
            val light = backgroundPixel()

            compose.onNodeWithText("Settings").performClick()
            compose.onNodeWithText("Dark").performClick()
            awaitMode(ThemeMode.DARK)
            compose.onNodeWithText("Library").performClick()
            compose.waitForIdle()
            val dark = backgroundPixel()

            assertNotEquals(light, dark)
        } finally {
            runBlocking { preferences.setThemeMode(ThemeMode.SYSTEM) }
        }
    }

    private fun awaitMode(mode: ThemeMode) {
        compose.waitUntil(5_000) {
            runBlocking { preferences.userPreferences.first().themeMode == mode }
        }
    }

    private fun backgroundPixel() =
        compose.onRoot().captureToImage().toPixelMap().let { pixels ->
            pixels[2, pixels.height / 2]
        }
}
