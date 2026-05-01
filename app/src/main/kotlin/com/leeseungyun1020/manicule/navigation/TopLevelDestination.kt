package com.leeseungyun1020.manicule.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.leeseungyun1020.manicule.R
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons

/**
 * 하단 탭 4개. plan.md 4. 화면 목록의 "하단 탭: 홈 / 서재 / 통계 / 설정" 정의.
 *
 * @property route               Navigation route 의 베이스 경로
 * @property iconSelected        탭이 활성화 됐을 때 보일 아이콘
 * @property iconUnselected      비활성 시 아이콘
 * @property labelRes            탭 라벨 (Strings.xml)
 */
enum class TopLevelDestination(
    val route: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
    val labelRes: Int,
) {
    HOME(
        route = "home",
        iconSelected = ManiculeIcons.Tab.HomeFilled,
        iconUnselected = ManiculeIcons.Tab.HomeOutlined,
        labelRes = R.string.tab_home,
    ),
    LIBRARY(
        route = "library",
        iconSelected = ManiculeIcons.Tab.LibraryFilled,
        iconUnselected = ManiculeIcons.Tab.LibraryOutlined,
        labelRes = R.string.tab_library,
    ),
    STATS(
        route = "stats",
        iconSelected = ManiculeIcons.Tab.StatsFilled,
        iconUnselected = ManiculeIcons.Tab.StatsOutlined,
        labelRes = R.string.tab_stats,
    ),
    SETTINGS(
        route = "settings",
        iconSelected = ManiculeIcons.Tab.SettingsFilled,
        iconUnselected = ManiculeIcons.Tab.SettingsOutlined,
        labelRes = R.string.tab_settings,
    ),
}
