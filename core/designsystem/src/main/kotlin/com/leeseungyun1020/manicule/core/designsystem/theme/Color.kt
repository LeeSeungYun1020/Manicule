package com.leeseungyun1020.manicule.core.designsystem.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Manicule 브랜드 컬러 — '읽음표' 컨셉을 살린 따뜻한 갈색 + 종이 톤.
 *
 * 잔디(ContributionGrid) 5단계 색상은 [GrassLight]/[GrassDark] 에서 별도로 정의한다.
 */
internal object ManiculeBrand {
    // Primary — 갈색 마뉴큘 인덱스 핑거 톤
    val Brown40 = Color(0xFF8C6A4A)
    val Brown20 = Color(0xFF513923)
    val Brown80 = Color(0xFFFFB68D)
    val Brown90 = Color(0xFFFFDCC4)

    // Secondary — 옅은 베이지
    val Beige40 = Color(0xFF7A5A41)
    val Beige20 = Color(0xFF59412C)
    val Beige80 = Color(0xFFE9C9AC)
    val Beige90 = Color(0xFFF8E6D2)

    // Tertiary — 종이 위 푸른 잉크
    val Ink40 = Color(0xFF4A5C7A)
    val Ink20 = Color(0xFF2D3D59)
    val Ink80 = Color(0xFFB6C7E5)
    val Ink90 = Color(0xFFD8E3F4)

    // Surface — 종이 색
    val Paper99 = Color(0xFFFFFBF6)
    val Paper95 = Color(0xFFF5EFE6)
    val Paper10 = Color(0xFF1A1410)
    val Paper20 = Color(0xFF2A231C)

    // Error
    val Error40 = Color(0xFFB3261E)
    val Error80 = Color(0xFFF2B8B5)
}

internal val LightColorScheme =
    lightColorScheme(
        primary = ManiculeBrand.Brown40,
        onPrimary = Color.White,
        primaryContainer = ManiculeBrand.Brown90,
        onPrimaryContainer = ManiculeBrand.Brown20,
        secondary = ManiculeBrand.Beige40,
        onSecondary = Color.White,
        secondaryContainer = ManiculeBrand.Beige90,
        onSecondaryContainer = ManiculeBrand.Beige20,
        tertiary = ManiculeBrand.Ink40,
        onTertiary = Color.White,
        tertiaryContainer = ManiculeBrand.Ink90,
        onTertiaryContainer = ManiculeBrand.Ink20,
        background = ManiculeBrand.Paper99,
        onBackground = ManiculeBrand.Paper10,
        surface = ManiculeBrand.Paper99,
        onSurface = ManiculeBrand.Paper10,
        surfaceVariant = ManiculeBrand.Paper95,
        onSurfaceVariant = ManiculeBrand.Paper20,
        error = ManiculeBrand.Error40,
        onError = Color.White,
    )

internal val DarkColorScheme =
    darkColorScheme(
        primary = ManiculeBrand.Brown80,
        onPrimary = ManiculeBrand.Brown20,
        primaryContainer = ManiculeBrand.Brown40,
        onPrimaryContainer = ManiculeBrand.Brown90,
        secondary = ManiculeBrand.Beige80,
        onSecondary = ManiculeBrand.Beige20,
        secondaryContainer = ManiculeBrand.Beige40,
        onSecondaryContainer = ManiculeBrand.Beige90,
        tertiary = ManiculeBrand.Ink80,
        onTertiary = ManiculeBrand.Ink20,
        tertiaryContainer = ManiculeBrand.Ink40,
        onTertiaryContainer = ManiculeBrand.Ink90,
        background = ManiculeBrand.Paper10,
        onBackground = ManiculeBrand.Paper95,
        surface = ManiculeBrand.Paper10,
        onSurface = ManiculeBrand.Paper95,
        surfaceVariant = ManiculeBrand.Paper20,
        onSurfaceVariant = ManiculeBrand.Paper95,
        error = ManiculeBrand.Error80,
        onError = ManiculeBrand.Paper10,
    )

/**
 * 잔디 5단계 색상.
 *
 * Index 0..4 — [com.leeseungyun1020.manicule.core.model.ContributionDay.intensity] 와 1:1 대응.
 */
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
        Color(0xFF2A231C),
        Color(0xFF513923),
        Color(0xFF7A5A41),
        Color(0xFFAE8662),
        Color(0xFFE9C9AC),
    )
