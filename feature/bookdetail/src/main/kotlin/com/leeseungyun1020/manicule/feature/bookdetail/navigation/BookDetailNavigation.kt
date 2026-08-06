@file:Suppress("ktlint:standard:filename")

package com.leeseungyun1020.manicule.feature.bookdetail.navigation

import kotlinx.serialization.Serializable

@Serializable
data class BookDetailRoute(
    val isbn: String,
)
