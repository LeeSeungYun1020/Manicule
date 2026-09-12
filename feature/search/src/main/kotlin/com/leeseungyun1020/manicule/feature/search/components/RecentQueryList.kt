package com.leeseungyun1020.manicule.feature.search.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeader
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.feature.search.R

@Composable
fun RecentQueryList(
    queries: List<String>,
    onQuerySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item(contentType = "section_header") {
            ManiculeSectionHeader(
                title = stringResource(R.string.search_recent_title),
            )
        }
        items(
            items = queries,
            key = { query -> query },
            contentType = { "recent_query" },
        ) { query ->
            QueryListItem(
                query = AnnotatedString(query),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                    )
                },
                onClick = { onQuerySelected(query) },
            )
        }
    }
}

@Composable
fun FilteredQueryList(
    queries: List<String>,
    query: String,
    onQuerySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        items(
            items = queries,
            key = { item -> item },
            contentType = { "filtered_query" },
        ) { item ->
            val highlightedQuery =
                remember(item, query) {
                    buildHighlightedQuery(item, query)
                }
            QueryListItem(
                query = highlightedQuery,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                },
                onClick = { onQuerySelected(item) },
            )
        }
    }
}

@Composable
private fun QueryListItem(
    query: AnnotatedString,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    ListItem(
        headlineContent = {
            Text(
                text = query,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        leadingContent = leadingIcon,
    )
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

private fun buildHighlightedQuery(
    recentQuery: String,
    input: String,
) = buildAnnotatedString {
    append(recentQuery)
    val start = recentQuery.indexOf(input, ignoreCase = true)
    if (start >= 0 && input.isNotEmpty()) {
        addStyle(
            style = SpanStyle(fontWeight = FontWeight.Bold),
            start = start,
            end = start + input.length,
        )
    }
}

@ManiculePreview
@Composable
private fun RecentQueryListPreview() {
    ManiculePreviewTheme {
        RecentQueryList(
            queries = listOf("Jetpack Compose", "Kotlin coroutines"),
            onQuerySelected = {},
        )
    }
}

@ManiculePreview
@Composable
private fun FilteredQueryListPreview() {
    ManiculePreviewTheme {
        FilteredQueryList(
            queries = listOf("Jetpack Compose", "Compose performance"),
            query = "compose",
            onQuerySelected = {},
        )
    }
}
