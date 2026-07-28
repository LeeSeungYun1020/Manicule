package com.leeseungyun1020.manicule.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal object ManiculePalette {
    // Brown — H67 C26
    val Brown20 = Color(0xFF462A0C)
    val Brown30 = Color(0xFF604021)
    val Brown34 = Color(0xFF6A492A)
    val Brown40 = Color(0xFF7A5738)
    val Brown48 = Color(0xFF8F6B4A)
    val Brown64 = Color(0xFFBB9471)
    val Brown66 = Color(0xFFC19976)
    val Brown80 = Color(0xFFE9BE9A)
    val Brown82 = Color(0xFFEFC4A0)
    val Brown90 = Color(0xFFFFDCC0)
    val Brown100 = Color(0xFFFFFFFF)

    // Neutral — H80 C4
    val Neutral0 = Color(0xFF000000)
    val Neutral4 = Color(PAPER_DARKEST_ARGB)
    val Neutral6 = Color(PAPER_DARK_ARGB)
    val Neutral10 = Color(0xFF1F1B16)
    val Neutral12 = Color(0xFF231F1A)
    val Neutral17 = Color(0xFF2D2924)
    val Neutral20 = Color(0xFF34302B)
    val Neutral22 = Color(0xFF38342F)
    val Neutral24 = Color(0xFF3D3933)
    val Neutral87 = Color(0xFFDFD9D2)
    val Neutral90 = Color(0xFFE7E2DB)
    val Neutral92 = Color(0xFFEDE7E1)
    val Neutral94 = Color(0xFFF3EDE6)
    val Neutral95 = Color(0xFFF5F0E9)
    val Neutral96 = Color(0xFFF8F3EC)
    val Neutral98 = Color(0xFFFEF8F2)
    val Neutral99 = Color(0xFFFFFCF7)
    val Neutral100 = Color(0xFFFFFFFF)

    // NeutralVariant — H78 C9
    val NVar20 = Color(0xFF382F24)
    val NVar30 = Color(0xFF4F4539)
    val NVar40 = Color(0xFF675D50)
    val NVar50 = Color(0xFF807568)
    val NVar60 = Color(0xFF9A8F81)
    val NVar80 = Color(0xFFD1C5B6)
    val NVar90 = Color(0xFFEDE1D2)
    val NVar100 = Color(0xFFFFFFFF)

    // Blue — H274 C22
    val Blue20 = Color(0xFF173150)
    val Blue24 = Color(0xFF223A5A)
    val Blue30 = Color(0xFF314869)
    val Blue40 = Color(0xFF495F82)
    val Blue60 = Color(0xFF7C91B7)
    val Blue80 = Color(0xFFB2C7EF)
    val Blue90 = Color(0xFFD6E3FF)
    val Blue100 = Color(0xFFFFFFFF)

    // Amber — H70 C52
    val Amber46 = Color(0xFF9A6014)
    val Amber74 = Color(0xFFEDA85C)

    // Red — H25 C50
    val Red20 = Color(0xFF670116)
    val Red30 = Color(0xFF881C29)
    val Red40 = Color(0xFFA5383E)
    val Red80 = Color(0xFFFFB3B0)
    val Red90 = Color(0xFFFEDAD8)
    val Red100 = Color(0xFFFFFFFF)
}

internal const val PAPER_DARK_ARGB = 0xFF17130C
internal const val PAPER_DARKEST_ARGB = 0xFF130D05

internal val LightColorScheme =
    lightColorScheme(
        primary = ManiculePalette.Brown40,
        onPrimary = ManiculePalette.Brown100,
        primaryContainer = ManiculePalette.Brown90,
        onPrimaryContainer = ManiculePalette.Brown30,
        inversePrimary = ManiculePalette.Brown80,
        secondary = ManiculePalette.NVar40,
        onSecondary = ManiculePalette.NVar100,
        secondaryContainer = ManiculePalette.NVar90,
        onSecondaryContainer = ManiculePalette.NVar30,
        tertiary = ManiculePalette.Blue40,
        onTertiary = ManiculePalette.Blue100,
        tertiaryContainer = ManiculePalette.Blue90,
        onTertiaryContainer = ManiculePalette.Blue30,
        background = ManiculePalette.Neutral99,
        onBackground = ManiculePalette.Neutral10,
        surface = ManiculePalette.Neutral99,
        onSurface = ManiculePalette.Neutral10,
        surfaceVariant = ManiculePalette.NVar90,
        onSurfaceVariant = ManiculePalette.NVar30,
        surfaceContainerLowest = ManiculePalette.Neutral100,
        surfaceContainerLow = ManiculePalette.Neutral96,
        surfaceContainer = ManiculePalette.Neutral94,
        surfaceContainerHigh = ManiculePalette.Neutral92,
        surfaceContainerHighest = ManiculePalette.Neutral90,
        surfaceDim = ManiculePalette.Neutral87,
        surfaceBright = ManiculePalette.Neutral98,
        outline = ManiculePalette.NVar50,
        outlineVariant = ManiculePalette.NVar80,
        error = ManiculePalette.Red40,
        onError = ManiculePalette.Red100,
        errorContainer = ManiculePalette.Red90,
        onErrorContainer = ManiculePalette.Red30,
        inverseSurface = ManiculePalette.Neutral20,
        inverseOnSurface = ManiculePalette.Neutral95,
        scrim = ManiculePalette.Neutral0,
        surfaceTint = ManiculePalette.Brown40,
    )

internal val DarkColorScheme =
    darkColorScheme(
        primary = ManiculePalette.Brown80,
        onPrimary = ManiculePalette.Brown20,
        primaryContainer = ManiculePalette.Brown30,
        onPrimaryContainer = ManiculePalette.Brown90,
        inversePrimary = ManiculePalette.Brown40,
        secondary = ManiculePalette.NVar80,
        onSecondary = ManiculePalette.NVar20,
        secondaryContainer = ManiculePalette.NVar30,
        onSecondaryContainer = ManiculePalette.NVar90,
        tertiary = ManiculePalette.Blue80,
        onTertiary = ManiculePalette.Blue20,
        tertiaryContainer = ManiculePalette.Blue30,
        onTertiaryContainer = ManiculePalette.Blue90,
        background = ManiculePalette.Neutral6,
        onBackground = ManiculePalette.Neutral90,
        surface = ManiculePalette.Neutral6,
        onSurface = ManiculePalette.Neutral90,
        surfaceVariant = ManiculePalette.NVar30,
        onSurfaceVariant = ManiculePalette.NVar80,
        surfaceContainerLowest = ManiculePalette.Neutral4,
        surfaceContainerLow = ManiculePalette.Neutral10,
        surfaceContainer = ManiculePalette.Neutral12,
        surfaceContainerHigh = ManiculePalette.Neutral17,
        surfaceContainerHighest = ManiculePalette.Neutral22,
        surfaceDim = ManiculePalette.Neutral6,
        surfaceBright = ManiculePalette.Neutral24,
        outline = ManiculePalette.NVar60,
        outlineVariant = ManiculePalette.NVar30,
        error = ManiculePalette.Red80,
        onError = ManiculePalette.Red20,
        errorContainer = ManiculePalette.Red30,
        onErrorContainer = ManiculePalette.Red90,
        inverseSurface = ManiculePalette.Neutral90,
        inverseOnSurface = ManiculePalette.Neutral20,
        scrim = ManiculePalette.Neutral0,
        surfaceTint = ManiculePalette.Brown80,
    )

val GrassLight: List<Color> =
    listOf(
        Color(0xFFEBE3D6),
        Color(0xFFE5C7A6),
        Color(0xFFCD9F6F),
        Color(0xFFA9763F),
        Color(0xFF6E4521),
    )

val GrassDark: List<Color> =
    listOf(
        Color(0xFF3D2F23),
        Color(0xFF513923),
        Color(0xFF7A5A41),
        Color(0xFFAE8662),
        Color(0xFFE9C9AC),
    )
