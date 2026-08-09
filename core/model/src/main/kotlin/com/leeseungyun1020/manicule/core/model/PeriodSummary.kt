package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

data class PeriodSummary(
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val longestStreak: Int,
    val pagesRead: Int,
    val bookCount: Int,
)
