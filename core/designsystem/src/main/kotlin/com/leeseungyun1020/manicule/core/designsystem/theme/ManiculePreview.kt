package com.leeseungyun1020.manicule.core.designsystem.theme

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
@Preview(name = "1. Light", uiMode = UI_MODE_NIGHT_NO, showBackground = true)
@Preview(name = "2. Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true, backgroundColor = PAPER_DARK_ARGB)
@Preview(name = "3. Font 1.5x", fontScale = 1.5f, showBackground = true)
annotation class ManiculePreview
