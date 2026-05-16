package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 연속 독서 일수 정보.
 *
 * 일일 누적 페이지 ≥ 1 인 날을 연속 카운트한다.
 *
 * @property current  현재 진행중인 streak (오늘 또는 어제까지 이어진 일 수)
 * @property longest  이력 중 최장 streak
 * @property lastDate streak 의 마지막 일자 — UI 메시지에 사용
 */
data class ReadingStreak(
    val current: Int,
    val longest: Int,
    val lastDate: LocalDate?,
) {
    companion object {
        val Empty: ReadingStreak = ReadingStreak(current = 0, longest = 0, lastDate = null)
    }
}
