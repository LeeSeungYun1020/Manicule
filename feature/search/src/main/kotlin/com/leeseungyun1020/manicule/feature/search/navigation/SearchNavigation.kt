package com.leeseungyun1020.manicule.feature.search.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.search.SearchRoute
import com.leeseungyun1020.manicule.feature.search.SearchScannerAction
import kotlinx.serialization.Serializable

@Serializable
object SearchRoute

fun NavGraphBuilder.searchScreen(
    onNavigateBack: () -> Unit,
    onBookSelected: (String) -> Unit,
    scannerAction: SearchScannerAction,
) {
    composable<SearchRoute> {
        SearchRoute(
            onNavigateBack = onNavigateBack,
            onBookSelected = onBookSelected,
            scannerAction = scannerAction,
        )
    }
}
