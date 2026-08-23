package com.leeseungyun1020.manicule.feature.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSearchBar
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeader
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    searchFieldState: TextFieldState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
                .padding(top = MaterialTheme.spacing.sm, bottom = MaterialTheme.spacing.xl),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        ManiculeSearchBar(
            state = searchFieldState,
            onSearch = {},
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.search_hint),
            requestInitialFocus = true,
            leadingIcon = {
                ManiculeIconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.search_back),
                    )
                }
            },
        )

        when (uiState) {
            SearchUiState.Loading -> SearchLoading()
            SearchUiState.Unavailable -> SearchEmpty()
            is SearchUiState.Content -> SearchContent(recentQueries = uiState.recentQueries)
        }
    }
}

@Composable
private fun SearchLoading() {
    val description = stringResource(R.string.search_loading)
    ManiculeLoading(
        modifier =
            Modifier
                .fillMaxSize()
                .semantics { contentDescription = description },
    )
}

@Composable
private fun SearchContent(recentQueries: List<String>) {
    if (recentQueries.isEmpty()) {
        SearchEmpty()
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(contentType = "section_header") {
            ManiculeSectionHeader(title = stringResource(R.string.search_recent_title))
        }
        items(
            items = recentQueries,
            key = { it },
            contentType = { "recent_query" },
        ) { query ->
            ListItem(
                headlineContent = {
                    Text(
                        text = query,
                        maxLines = 2,
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                    )
                },
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun SearchEmpty() {
    ManiculeEmptyState(
        title = stringResource(R.string.search_empty_title),
        description = stringResource(R.string.search_empty_description),
        modifier = Modifier.fillMaxSize(),
        icon = {
            Icon(
                imageVector = ManiculeIcons.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(MaterialTheme.size.iconEmptyState),
            )
        },
    )
}

@ManiculePreview
@Preview(name = "Foldable", widthDp = 673, heightDp = 900, showBackground = true)
@Preview(name = "Tablet", widthDp = 1200, heightDp = 900, showBackground = true)
@Composable
private fun EmptySearchScreenPreview() {
    ManiculePreviewTheme {
        SearchScreen(
            uiState = SearchUiState.Content(emptyList()),
            searchFieldState = rememberTextFieldState(),
            onNavigateBack = {},
        )
    }
}

@ManiculePreview
@Composable
private fun UnavailableSearchScreenPreview() {
    ManiculePreviewTheme {
        SearchScreen(
            uiState = SearchUiState.Unavailable,
            searchFieldState = rememberTextFieldState(),
            onNavigateBack = {},
        )
    }
}

@ManiculePreview
@Composable
private fun RecentSearchScreenPreview() {
    ManiculePreviewTheme {
        SearchScreen(
            uiState =
                SearchUiState.Content(
                    listOf(
                        "Jetpack Compose",
                        "A very long search query that wraps onto another line for accessibility",
                        "Kotlin coroutines",
                    ),
                ),
            searchFieldState = rememberTextFieldState(),
            onNavigateBack = {},
        )
    }
}

@ManiculePreview
@Composable
private fun LoadingSearchScreenPreview() {
    ManiculePreviewTheme {
        SearchScreen(
            uiState = SearchUiState.Loading,
            searchFieldState = rememberTextFieldState(),
            onNavigateBack = {},
        )
    }
}
