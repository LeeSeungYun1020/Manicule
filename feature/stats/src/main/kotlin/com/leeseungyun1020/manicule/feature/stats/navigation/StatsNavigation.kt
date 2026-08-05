@file:Suppress("ktlint:standard:filename")

package com.leeseungyun1020.manicule.feature.stats.navigation

import kotlinx.serialization.Serializable

@Serializable
data class StatsRoute(
    val focus: String? = null,
)
