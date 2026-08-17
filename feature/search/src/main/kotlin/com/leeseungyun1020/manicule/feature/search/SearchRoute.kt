package com.leeseungyun1020.manicule.feature.search

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val searchFieldState = rememberTextFieldState()

    SearchScreen(
        uiState = uiState.value,
        searchFieldState = searchFieldState,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
