package com.leeseungyun1020.manicule.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.leeseungyun1020.manicule.core.designsystem.R

/**
 * Google Fonts (Downloadable Fonts) 를 사용하는 Noto Sans KR 폰트 패밀리.
 *
 * `core:designsystem` 모듈에는 prefetch 를 위한 res/values/font_certs.xml 가 필요하다.
 */
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val notoSansKr = FontFamily(
    Font(GoogleFont("Noto Sans KR"), provider, FontWeight.Light),
    Font(GoogleFont("Noto Sans KR"), provider, FontWeight.Normal),
    Font(GoogleFont("Noto Sans KR"), provider, FontWeight.Medium),
    Font(GoogleFont("Noto Sans KR"), provider, FontWeight.SemiBold),
    Font(GoogleFont("Noto Sans KR"), provider, FontWeight.Bold),
)

/**
 * Manicule 의 Typography. Material3 디폴트를 한국어 본문 가독성에 맞춰
 * line-height 를 조정한 형태.
 */
val ManiculeTypography: Typography = Typography().run {
    copy(
        displayLarge = displayLarge.kr(weight = FontWeight.SemiBold),
        displayMedium = displayMedium.kr(weight = FontWeight.SemiBold),
        displaySmall = displaySmall.kr(weight = FontWeight.SemiBold),
        headlineLarge = headlineLarge.kr(weight = FontWeight.SemiBold),
        headlineMedium = headlineMedium.kr(weight = FontWeight.SemiBold),
        headlineSmall = headlineSmall.kr(weight = FontWeight.SemiBold),
        titleLarge = titleLarge.kr(weight = FontWeight.Medium, lineHeight = 30.sp),
        titleMedium = titleMedium.kr(weight = FontWeight.Medium, lineHeight = 24.sp),
        titleSmall = titleSmall.kr(weight = FontWeight.Medium),
        bodyLarge = bodyLarge.kr(lineHeight = 26.sp),
        bodyMedium = bodyMedium.kr(lineHeight = 22.sp),
        bodySmall = bodySmall.kr(lineHeight = 18.sp),
        labelLarge = labelLarge.kr(weight = FontWeight.Medium),
        labelMedium = labelMedium.kr(weight = FontWeight.Medium),
        labelSmall = labelSmall.kr(weight = FontWeight.Medium),
    )
}

private fun TextStyle.kr(
    weight: FontWeight = fontWeight ?: FontWeight.Normal,
    lineHeight: androidx.compose.ui.unit.TextUnit = this.lineHeight,
): TextStyle = copy(
    fontFamily = notoSansKr,
    fontWeight = weight,
    lineHeight = lineHeight,
)
