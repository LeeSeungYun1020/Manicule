package com.leeseungyun1020.manicule.core.designsystem.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ManiculeSpacing {
    val xxs: Dp = 2.dp
    val xs: Dp = 4.dp
    val sm: Dp = 8.dp
    val md: Dp = 12.dp
    val lg: Dp = 16.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 40.dp

    val screenHorizontal: Dp = lg
    val screenContent: PaddingValues = PaddingValues(start = lg, end = lg, top = sm, bottom = xxl)
}

object ManiculeSize {
    val iconXs: Dp = 16.dp
    val iconSm: Dp = 20.dp
    val iconMd: Dp = 24.dp
    val iconLg: Dp = 32.dp

    val iconEmptyState: Dp = 48.dp

    val touchTargetMin: Dp = 48.dp

    val calendarCell: Dp = 20.dp
    val calendarCellGap: Dp = 4.dp

    val contentMaxWidth: Dp = 600.dp
}

object ManiculeBorder {
    val hairline: Dp = 1.dp
    val dashed: Dp = 2.dp
    val ring: Dp = 2.dp

    val dashOn: Dp = 6.dp
    val dashOff: Dp = 6.dp
}

val MaterialTheme.spacing: ManiculeSpacing get() = ManiculeSpacing
val MaterialTheme.size: ManiculeSize get() = ManiculeSize
val MaterialTheme.border: ManiculeBorder get() = ManiculeBorder

val MaterialTheme.maniculeColors: ManiculeExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalManiculeColors.current
