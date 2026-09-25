package com.leeseungyun1020.manicule.feature.stats.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeBottomSheet
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeErrorState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSnackbarHost
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.ui.book.BookListItem
import com.leeseungyun1020.manicule.feature.stats.DayState
import com.leeseungyun1020.manicule.feature.stats.R
import kotlinx.datetime.LocalDate

@Composable
fun ReadingDayBottomSheet(
    state: DayState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onBookSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState? = null,
) {
    if (state == DayState.Closed) return
    ManiculeBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ReadingDayContent(state = state, onDismiss = onDismiss, onRetry = onRetry, onBookSelected = onBookSelected)
            if (snackbarHostState != null) {
                ManiculeSnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun ReadingDayContent(
    state: DayState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onBookSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val date = when (state) {
        is DayState.Loading -> state.date
        is DayState.Content -> state.date
        is DayState.Error -> state.date
        DayState.Closed -> return
    }
    Column(modifier = modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.lg)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = MaterialTheme.spacing.lg, end = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = if (state is DayState.Content) {
                    pluralStringResource(
                        R.plurals.stats_day_title,
                        state.rows.size,
                        date.year,
                        date.monthNumber,
                        date.dayOfMonth,
                        state.rows.size,
                    )
                } else {
                    stringResource(R.string.stats_day_loading_title, date.year, date.monthNumber, date.dayOfMonth)
                },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onDismiss) {
                Icon(ManiculeIcons.Close, contentDescription = stringResource(R.string.stats_close))
            }
        }
        when (state) {
            is DayState.Loading -> ManiculeLoading(modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.xl))
            is DayState.Error -> DayError(onRetry)
            is DayState.Content -> {
                if (state.rows.isEmpty()) {
                    Text(
                        text = stringResource(R.string.stats_day_empty),
                        modifier = Modifier.padding(MaterialTheme.spacing.lg),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = ManiculeSize.chartHeight * 2)) {
                        items(state.rows, key = { it.isbn }, contentType = { "reading-day-book" }) { row ->
                            val book = row.book
                            BookListItem(
                                title = book?.title ?: stringResource(R.string.stats_book_missing),
                                author = book?.author.orEmpty(),
                                publisher = book?.publisher.orEmpty(),
                                pubDate = book?.publishedDate?.toString().orEmpty(),
                                imageUrl = book?.coverUrl,
                                modifier = Modifier.clickable(role = Role.Button) { onBookSelected(row.isbn) },
                                trailingContent = {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = pluralStringResource(R.plurals.stats_pages_value, row.pagesRead, row.pagesRead),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            text = pluralStringResource(R.plurals.stats_session_count, row.recordCount, row.recordCount),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
            DayState.Closed -> Unit
        }
    }
}

@Composable
private fun DayError(onRetry: () -> Unit) {
    ManiculeErrorState(
        title = stringResource(R.string.stats_day_error),
        icon = ManiculeIcons.NetworkError,
        onRetry = onRetry,
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
    )
}

@ManiculePreview
@Composable
private fun ReadingDayContentPreview() {
    ManiculePreviewTheme {
        ReadingDayContent(DayState.Content(LocalDate(2026, 9, 24), emptyList()), {}, {}, {})
    }
}
