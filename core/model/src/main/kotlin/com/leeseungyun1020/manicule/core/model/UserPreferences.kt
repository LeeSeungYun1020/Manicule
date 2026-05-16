package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalTime

/**
 * 시스템 / 라이트 / 다크 테마 모드.
 */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * 독서 리마인더 알림 설정.
 *
 * @property enabled 알림 사용 여부
 * @property time    매일 알림 발송 시각 (로컬 시간)
 */
data class ReminderConfig(
    val enabled: Boolean,
    val time: LocalTime,
) {
    companion object {
        /** 기본값: off, 21:00. */
        val Default: ReminderConfig =
            ReminderConfig(
                enabled = false,
                time = LocalTime(21, 0),
            )
    }
}

/**
 * 사용자 환경설정.
 */
data class UserPreferences(
    val themeMode: ThemeMode,
    val reminder: ReminderConfig,
) {
    companion object {
        val Default: UserPreferences =
            UserPreferences(
                themeMode = ThemeMode.SYSTEM,
                reminder = ReminderConfig.Default,
            )
    }
}
