package com.leeseungyun1020.manicule.core.database.dao.projection

import kotlinx.datetime.LocalDate

data class DailyReadingProjection(
    val date: LocalDate,
    val pagesRead: Int,
    val bookCount: Int,
)
