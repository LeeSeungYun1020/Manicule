package com.leeseungyun1020.manicule.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Manicule 에서 쓰는 아이콘을 한 곳에서 모아 관리한다.
 *
 * Compose Material Icons (extended) 의 원본 ImageVector 를 alias 로만 노출하여
 * 추후 커스텀 아이콘으로 교체가 쉬워지도록 한다.
 */
object ManiculeIcons {
    val Add: ImageVector = Icons.Default.Add
    val Search: ImageVector = Icons.Default.Search
    val Camera: ImageVector = Icons.Default.CameraAlt
    val ScanBarcode: ImageVector = Icons.Default.QrCodeScanner
    val Edit: ImageVector = Icons.Default.Edit
    val Delete: ImageVector = Icons.Default.Delete
    val Star: ImageVector = Icons.Default.Star
    val StarBorder: ImageVector = Icons.Default.StarBorder

    object Tab {
        val HomeFilled: ImageVector = Icons.Filled.Home
        val HomeOutlined: ImageVector = Icons.Outlined.Home
        val LibraryFilled: ImageVector = Icons.AutoMirrored.Filled.MenuBook
        val LibraryOutlined: ImageVector = Icons.AutoMirrored.Outlined.MenuBook
        val StatsFilled: ImageVector = Icons.Filled.BarChart
        val StatsOutlined: ImageVector = Icons.Outlined.BarChart
        val SettingsFilled: ImageVector = Icons.Filled.Settings
        val SettingsOutlined: ImageVector = Icons.Outlined.Settings
    }
}
