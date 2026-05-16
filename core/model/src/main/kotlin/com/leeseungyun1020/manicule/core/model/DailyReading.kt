package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 통계용 — 특정 날짜에 읽은 페이지 수의 합계.
 *
 * 잔디(ContributionGrid)의 단위 데이터.
 */
data class DailyReading(
    val date: LocalDate,
    val pages: Int,
)
