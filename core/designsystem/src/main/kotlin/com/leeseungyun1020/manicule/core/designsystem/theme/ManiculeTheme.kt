package com.leeseungyun1020.manicule.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/**
 * Manicule 의 최상위 테마.
 *
 * @param darkTheme 다크 모드 강제 적용 여부. null/false 면 시스템 다크 여부에 따른다.
 */
@Composable
fun ManiculeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalManiculeColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ManiculeTypography,
            shapes = ManiculeShapes,
            content = content,
        )
    }
}
