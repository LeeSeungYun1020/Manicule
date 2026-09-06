package com.leeseungyun1020.manicule.feature.search

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SearchRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchFieldState = rememberTextFieldState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(searchFieldState, viewModel) {
        snapshotFlow { searchFieldState.text.toString() }
            .distinctUntilChanged()
            .collect { query -> viewModel.onQueryChanged(query) }
    }

    val submitSearch: (String) -> Unit = { query ->
        val normalizedQuery = query.trim()
        if (normalizedQuery.isNotEmpty()) {
            searchFieldState.setTextAndPlaceCursorAtEnd(normalizedQuery)
            viewModel.onSearch(normalizedQuery)
            keyboardController?.hide()
        }
    }

    SearchScreen(
        uiState = uiState,
        searchResults = viewModel.searchResults,
        searchFieldState = searchFieldState,
        onSearch = submitSearch,
        onQuerySelected = submitSearch,
        onNavigateBack = onNavigateBack,
        modifier = modifier,
    )
}
