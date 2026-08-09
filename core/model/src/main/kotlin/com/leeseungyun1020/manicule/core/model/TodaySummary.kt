package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

data class TodaySummary(
    val date: LocalDate,
    val pagesRead: Int,
    val bookCount: Int,
)
