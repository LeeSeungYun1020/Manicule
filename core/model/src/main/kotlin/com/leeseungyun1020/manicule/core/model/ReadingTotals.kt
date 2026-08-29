package com.leeseungyun1020.manicule.core.model

/**
 * 지정 기간 전체의 독서 집계.
 *
 * @property pagesRead 기간 내 읽은 총 페이지 수
 * @property bookCount 기간 내 읽은 고유 도서 수
 */
data class ReadingTotals(
    val pagesRead: Int,
    val bookCount: Int,
)
