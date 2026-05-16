package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 한 책에 대한 날짜별 누적 페이지 기록.
 *
 * "4월 24일 — 100쪽까지" 처럼 그 날짜까지 읽은 누적 페이지 수를 저장한다.
 * 당일 읽은 페이지 수는 이전 기록과의 차이로 계산한다.
 */
data class ReadingRecord(
    val id: Long,
    val isbn: String,
    val date: LocalDate,
    val cumulativePage: Int,
) {
    init {
        require(cumulativePage >= 0) {
            "cumulativePage must be non-negative, was $cumulativePage"
        }
    }
}
