package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 특정 날짜의 독서 집계.
 *
 * @property date 집계 대상 날짜
 * @property pagesRead 해당 날짜에 읽은 총 페이지 수
 * @property bookCount 해당 날짜에 읽은 고유 도서 수
 */
data class DailyReading(
    val date: LocalDate,
    val pagesRead: Int,
    val bookCount: Int,
)
