package com.leeseungyun1020.manicule.feature.bookdetail

import kotlinx.serialization.Serializable

@Serializable
data class BookDetailRoute(
    val isbn: String,
    val openMyRecords: Boolean = false,
)
