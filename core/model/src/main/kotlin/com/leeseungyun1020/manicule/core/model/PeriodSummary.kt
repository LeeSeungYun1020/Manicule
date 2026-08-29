package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 포함 기간의 독서 요약.
 *
 * @property rangeStart 집계 시작 날짜
 * @property rangeEnd 집계 종료 날짜
 * @property longestStreak 기간 내 최장 연속 기록 일수
 * @property pagesRead 기간 내 읽은 총 페이지 수
 * @property bookCount 기간 내 읽은 고유 도서 수
 */
data class PeriodSummary(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val longestStreak: Int,
    val pagesRead: Int,
    val bookCount: Int,
)
