package com.leeseungyun1020.manicule.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.search.SearchRoute
import kotlinx.serialization.Serializable

@Serializable
object SearchRoute

fun NavGraphBuilder.searchScreen(
    onNavigateBack: () -> Unit = {},
    onBookSelected: (String) -> Unit = {},
    onNavigateToScanner: () -> Unit = {},
) {
    composable<SearchRoute> {
        SearchRoute(onNavigateBack = onNavigateBack)
    }
}
