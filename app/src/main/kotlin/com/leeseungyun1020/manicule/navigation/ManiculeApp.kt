package com.leeseungyun1020.manicule.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

/**
 * Scaffold + BottomBar 를 포함한 최상위 컴포저블.
 */
@Composable
fun ManiculeApp(appState: ManiculeAppState) {
    val currentTopLevelDestination = appState.currentTopLevelDestination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentTopLevelDestination != null) {
                ManiculeBottomBar(
                    destinations = appState.topLevelDestinations,
                    currentDestination = currentTopLevelDestination,
                    onNavigateToDestination = appState::navigateToTopLevelDestination,
                )
            }
        },
    ) { padding ->
        ManiculeNavHost(appState = appState, paddingValues = padding)
    }
}

@Composable
private fun ManiculeBottomBar(
    destinations: List<TopLevelDestination>,
    currentDestination: TopLevelDestination,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        destinations.forEach { destination ->
            val selected = destination == currentDestination
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigateToDestination(destination) },
                icon = {
                    Icon(
                        imageVector =
                            if (selected) {
                                destination.iconSelected
                            } else {
                                destination.iconUnselected
                            },
                        contentDescription = null,
                    )
                },
                label = { Text(text = stringResource(destination.labelRes)) },
            )
        }
    }
}
