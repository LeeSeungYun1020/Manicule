package com.leeseungyun1020.manicule.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.leeseungyun1020.manicule.feature.settings.LicensesRoute
import com.leeseungyun1020.manicule.feature.settings.SettingsRoute
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

@Serializable
object LicensesRoute

fun NavGraphBuilder.settingsScreen(onNavigateToLicenses: () -> Unit) {
    composable<SettingsRoute> {
        SettingsRoute(onNavigateToLicenses = onNavigateToLicenses)
    }
}

fun NavGraphBuilder.licensesScreen(onNavigateBack: () -> Unit) {
    composable<LicensesRoute> {
        LicensesRoute(onNavigateBack = onNavigateBack)
    }
}
