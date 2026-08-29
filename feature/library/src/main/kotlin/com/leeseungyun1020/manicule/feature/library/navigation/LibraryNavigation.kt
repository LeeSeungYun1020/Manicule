package com.leeseungyun1020.manicule.feature.library.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.library.LibraryRoute
import kotlinx.serialization.Serializable

@Serializable
object LibraryRoute

fun NavGraphBuilder.libraryScreen(
    onNavigateToBookDetail: (isbn: String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToScanner: () -> Unit,
) {
    composable<LibraryRoute> {
        LibraryRoute(
            onNavigateToBookDetail = onNavigateToBookDetail,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToScanner = onNavigateToScanner,
        )
    }
}
