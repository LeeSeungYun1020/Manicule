package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * 한 책을 읽은 세션 기록.
 */
data class ReadingRecord(
    val id: Long,
    val isbn: String,
    val date: LocalDate,
    val time: LocalTime,
    val startPage: Int,
    val endPage: Int,
) {
    val pagesRead: Int
        get() = endPage - startPage + 1

    init {
        require(startPage >= 1) {
            "startPage must be at least 1, was $startPage"
        }
        require(endPage >= startPage) {
            "endPage must be at least startPage, was $endPage"
        }
    }
}
