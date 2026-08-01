package com.leeseungyun1020.manicule.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ManiculeExtendedColors(
    val calendarLevels: List<Color>,
    val calendarPlaceholder: Color,
    val chartLine: Color,
    val streakAccent: Color,
    val onCoverOverlay: Color,
)

internal val LightExtendedColors =
    ManiculeExtendedColors(
        calendarLevels = listOf(
            ManiculePalette.Neutral92,
            ManiculePalette.Brown82,
            ManiculePalette.Brown66,
            ManiculePalette.Brown48,
            ManiculePalette.Brown30,
        ),
        calendarPlaceholder = Color.Transparent,
        chartLine = ManiculePalette.Blue24,
        streakAccent = ManiculePalette.Amber46,
        onCoverOverlay = ManiculePalette.Neutral100,
    )

internal val DarkExtendedColors =
    ManiculeExtendedColors(
        calendarLevels = listOf(
            ManiculePalette.Neutral20,
            ManiculePalette.Brown34,
            ManiculePalette.Brown48,
            ManiculePalette.Brown64,
            ManiculePalette.Brown82,
        ),
        calendarPlaceholder = Color.Transparent,
        chartLine = ManiculePalette.Blue60,
        streakAccent = ManiculePalette.Amber74,
        onCoverOverlay = ManiculePalette.Neutral100,
    )

internal val LocalManiculeColors =
    staticCompositionLocalOf { LightExtendedColors }
