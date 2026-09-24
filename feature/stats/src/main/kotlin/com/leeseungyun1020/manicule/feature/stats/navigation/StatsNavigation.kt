package com.leeseungyun1020.manicule.feature.stats.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.stats.StatsScreenRoute
import kotlinx.serialization.Serializable

@Serializable
data class StatsRoute(
    val focus: String? = null,
)

fun NavGraphBuilder.statsScreen() {
    composable<StatsRoute> { StatsScreenRoute() }
}
