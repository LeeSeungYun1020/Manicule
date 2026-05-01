package com.leeseungyun1020.manicule.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

/**
 * Manicule 의 최상위 NavHost.
 *
 * 1단계 단계에서는 stub 으로 전체 destination 을 등록만 해두고, 후속 단계에서
 * 각 feature 모듈의 `NavGraphBuilder.<name>Screen()` 으로 교체한다.
 */
@Composable
fun ManiculeNavHost(
    appState: ManiculeAppState,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(0.dp),
) {
    NavHost(
        navController = appState.navController,
        startDestination = TopLevelDestination.HOME.route,
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        composable(TopLevelDestination.HOME.route) { StubScreen("홈") }
        composable(TopLevelDestination.LIBRARY.route) { StubScreen("서재") }
        composable(TopLevelDestination.STATS.route) { StubScreen("통계") }
        composable(TopLevelDestination.SETTINGS.route) { StubScreen("설정") }
    }
}

@Composable
private fun StubScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "$label\n(stub — 후속 단계에서 구현)",
            style = MaterialTheme.typography.titleMedium,
        )
    }
}
