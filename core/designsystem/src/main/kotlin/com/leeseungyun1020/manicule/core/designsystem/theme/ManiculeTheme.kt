package com.leeseungyun1020.manicule.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 잔디 5단계 색상에 접근하기 위한 CompositionLocal.
 */
val LocalGrassColors = staticCompositionLocalOf<List<Color>> { GrassLight }

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
    val grassColors = if (darkTheme) GrassDark else GrassLight

    CompositionLocalProvider(LocalGrassColors provides grassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ManiculeTypography,
            shapes = ManiculeShapes,
            content = content,
        )
    }
}
