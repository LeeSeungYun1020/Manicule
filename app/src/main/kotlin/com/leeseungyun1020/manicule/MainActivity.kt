package com.leeseungyun1020.manicule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.domain.settings.GetUserPreferencesUseCase
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.navigation.ManiculeApp
import com.leeseungyun1020.manicule.navigation.rememberManiculeAppState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var getUserPreferences: GetUserPreferencesUseCase

    private var themeMode by mutableStateOf<ThemeMode?>(null)

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().setKeepOnScreenCondition { themeMode == null }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getUserPreferences()
                    .retryWhen { _, attempt ->
                        if (themeMode == null) themeMode = ThemeMode.SYSTEM
                        delay((attempt + 1).coerceAtMost(30) * 1_000)
                        true
                    }
                    .collect { themeMode = it.themeMode }
            }
        }
        setContent {
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                else -> systemDark
            }
            SideEffect {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
            ManiculeTheme(
                darkTheme = darkTheme,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize().testTag(APP_THEME_SURFACE_TAG),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    val windowSizeClass = calculateWindowSizeClass(this)
                    val appState = rememberManiculeAppState(windowSizeClass = windowSizeClass)
                    ManiculeApp(appState = appState)
                }
            }
        }
    }
}

internal const val APP_THEME_SURFACE_TAG = "app_theme_surface"
