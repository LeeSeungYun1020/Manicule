package com.leeseungyun1020.manicule.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.leeseungyun1020.manicule.R
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.feature.home.navigation.HomeRoute
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryRoute
import com.leeseungyun1020.manicule.feature.settings.navigation.SettingsRoute
import com.leeseungyun1020.manicule.feature.stats.navigation.StatsRoute

/**
 * 하단 탭 4개. plan.md 4. 화면 목록의 "하단 탭: 홈 / 서재 / 통계 / 설정" 정의.
 *
 * @property route               feature가 소유하는 타입 안전 navigation route 인스턴스
 * @property iconSelected        탭이 활성화 됐을 때 보일 아이콘
 * @property iconUnselected      비활성 시 아이콘
 * @property labelRes            탭 라벨 (Strings.xml)
 */
enum class TopLevelDestination(
    val route: Any,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
    val labelRes: Int,
) {
    HOME(
        route = HomeRoute,
        iconSelected = ManiculeIcons.Tab.HomeFilled,
        iconUnselected = ManiculeIcons.Tab.HomeOutlined,
        labelRes = R.string.tab_home,
    ),
    LIBRARY(
        route = LibraryRoute(),
        iconSelected = ManiculeIcons.Tab.LibraryFilled,
        iconUnselected = ManiculeIcons.Tab.LibraryOutlined,
        labelRes = R.string.tab_library,
    ),
    STATS(
        route = StatsRoute(),
        iconSelected = ManiculeIcons.Tab.StatsFilled,
        iconUnselected = ManiculeIcons.Tab.StatsOutlined,
        labelRes = R.string.tab_stats,
    ),
    SETTINGS(
        route = SettingsRoute,
        iconSelected = ManiculeIcons.Tab.SettingsFilled,
        iconUnselected = ManiculeIcons.Tab.SettingsOutlined,
        labelRes = R.string.tab_settings,
    ),
}
