package com.leeseungyun1020.manicule.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ManiculeExtendedColors(
    val calendarLevels: List<Color>,
    val calendarPlaceholder: Color,
    val chartBar: Color,
    val chartLine: Color,
    val chartLineHalo: Color,
    val chartGridline: Color,
    val chartAxisLabel: Color,
    val streakAccent: Color,
    val dashedBorder: Color,
    val coverPlaceholder: Color,
    val coverBorder: Color,
    val coverOverlayScrim: Color,
    val onCoverOverlay: Color,
    val cameraOverlay: Color,
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
        chartBar = ManiculePalette.Brown40,
        chartLine = ManiculePalette.Blue24,
        chartLineHalo = ManiculePalette.Neutral96,
        chartGridline = ManiculePalette.NVar80,
        chartAxisLabel = ManiculePalette.NVar30,
        streakAccent = ManiculePalette.Amber46,
        dashedBorder = ManiculePalette.NVar50,
        coverPlaceholder = ManiculePalette.NVar90,
        coverBorder = ManiculePalette.NVar80,
        coverOverlayScrim = ManiculePalette.Neutral0.copy(alpha = 0.60f),
        onCoverOverlay = ManiculePalette.Neutral100,
        cameraOverlay = ManiculePalette.Neutral0.copy(alpha = 0.60f),
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
        chartBar = ManiculePalette.Brown80,
        chartLine = ManiculePalette.Blue60,
        chartLineHalo = ManiculePalette.Neutral10,
        chartGridline = ManiculePalette.NVar30,
        chartAxisLabel = ManiculePalette.NVar80,
        streakAccent = ManiculePalette.Amber74,
        dashedBorder = ManiculePalette.NVar60,
        coverPlaceholder = ManiculePalette.NVar30,
        coverBorder = ManiculePalette.NVar30,
        coverOverlayScrim = ManiculePalette.Neutral0.copy(alpha = 0.60f),
        onCoverOverlay = ManiculePalette.Neutral100,
        cameraOverlay = ManiculePalette.Neutral0.copy(alpha = 0.60f),
    )

internal val LocalManiculeColors =
    staticCompositionLocalOf { LightExtendedColors }
