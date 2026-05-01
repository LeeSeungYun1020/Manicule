package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 홈 화면의 오늘 요약 통계.
 *
 * @property date  요약 대상 일자
 * @property pages 그 날 읽은 페이지 수
 */
data class TodaySummary(
    val date: LocalDate,
    val pages: Int,
)
