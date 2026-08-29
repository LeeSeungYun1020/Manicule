package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 로컬 날짜 기준 오늘의 독서 요약.
 *
 * @property date 요약 대상 날짜
 * @property pagesRead 해당 날짜에 읽은 총 페이지 수
 * @property bookCount 해당 날짜에 읽은 고유 도서 수
 */
data class TodaySummary(
    val date: LocalDate,
    val pagesRead: Int,
    val bookCount: Int,
)
