package com.leeseungyun1020.manicule.feature.scanner.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.scanner.ScannerRoute
import kotlinx.serialization.Serializable

@Serializable
object ScannerRoute

fun NavGraphBuilder.scannerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
) {
    composable<ScannerRoute> {
        ScannerRoute(
            onNavigateBack = onNavigateBack,
            onNavigateToSearch = onNavigateToSearch,
        )
    }
}
