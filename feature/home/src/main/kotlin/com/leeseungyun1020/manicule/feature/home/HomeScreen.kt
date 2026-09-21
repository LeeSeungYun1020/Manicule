@file:Suppress("TooManyFunctions")

package com.leeseungyun1020.manicule.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeDashedCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeOutlinedButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSearchEntry
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeader
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeaderAction
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeStatTile
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.domain.home.HomeData
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.ui.book.BookCover
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize
import com.leeseungyun1020.manicule.core.ui.book.BookProgressBar
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarGrid
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarLegend
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearch: () -> Unit,
    onScan: (() -> Unit)?,
    onBookSelected: (String) -> Unit,
    onShowReadingBooks: () -> Unit,
    onChooseWantBook: () -> Unit,
    onShowStats: (() -> Unit)?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { ManiculeTopAppBar(title = stringResource(R.string.home_title)) },
    ) { padding ->
        when (uiState) {
            HomeUiState.Loading -> ManiculeLoading(Modifier.fillMaxSize().padding(padding))
            HomeUiState.Error -> HomeError(padding, onRetry)
            is HomeUiState.Content ->
                HomeContent(
                    data = uiState.data,
                    isFirstUser = uiState.isFirstUser,
                    onSearch = onSearch,
                    onScan = onScan,
                    onBookSelected = onBookSelected,
                    onShowReadingBooks = onShowReadingBooks,
                    onChooseWantBook = onChooseWantBook,
                    onShowStats = onShowStats,
                    modifier = Modifier.padding(padding),
                )
        }
    }
}

@Composable
private fun HomeContent(
    data: HomeData,
    isFirstUser: Boolean,
    onSearch: () -> Unit,
    onScan: (() -> Unit)?,
    onBookSelected: (String) -> Unit,
    onShowReadingBooks: () -> Unit,
    onChooseWantBook: () -> Unit,
    onShowStats: (() -> Unit)?,
    modifier: Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(ManiculeSpacing.screenContent),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl),
    ) {
        HomeSearchEntry(onSearch, onScan)
        if (isFirstUser) {
            OnboardingContent(onSearch, onScan)
        } else {
            ReadingSummary(data, onShowStats)
            if (data.readingBooks.isEmpty()) {
                NoReadingBooks(data.wantBookCount, onSearch, onScan, onChooseWantBook)
            } else {
                ReadingBooks(data.readingBooks, onBookSelected, onShowReadingBooks)
            }
        }
    }
}

@Composable
private fun HomeSearchEntry(
    onSearch: () -> Unit,
    onScan: (() -> Unit)?,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        ManiculeSearchEntry(
            onClick = onSearch,
            placeholder = stringResource(R.string.home_search_placeholder),
            modifier = Modifier.weight(1f),
            leadingIcon = { Icon(ManiculeIcons.Search, null) },
        )
        ManiculeIconButton(
            onClick = onScan ?: {},
            enabled = onScan != null,
            icon = {
                Icon(
                    imageVector = ManiculeIcons.ScanBarcode,
                    contentDescription = stringResource(R.string.home_scan),
                )
            },
        )
    }
}

@Composable
private fun OnboardingContent(
    onSearch: () -> Unit,
    onScan: (() -> Unit)?,
) {
    ManiculeDashedCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Text(stringResource(R.string.home_empty_summary_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.home_empty_summary_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    ManiculeCard {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Text(stringResource(R.string.home_onboarding_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.home_onboarding_step_one), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.home_onboarding_step_two), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.home_onboarding_step_three), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                ManiculeButton(onClick = onSearch, text = stringResource(R.string.home_search))
                ManiculeOutlinedButton(
                    onClick = onScan ?: {},
                    enabled = onScan != null,
                    text = stringResource(R.string.home_scan),
                )
            }
        }
    }
}

@Composable
private fun ReadingSummary(
    data: HomeData,
    onShowStats: (() -> Unit)?,
) {
    ManiculeCard(
        modifier =
            if (onShowStats != null) Modifier.clickable(onClick = onShowStats) else Modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Text(stringResource(R.string.home_reading_summary), style = MaterialTheme.typography.titleMedium)
            ReadingCalendarGrid(days = data.recentDays, today = data.today)
            ReadingCalendarLegend(modifier = Modifier.fillMaxWidth(), compact = true)
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                ManiculeStatTile(
                    value = pluralStringResource(R.plurals.home_days, data.currentStreak, data.currentStreak),
                    label = stringResource(R.string.home_streak),
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.LocalFireDepartment, null, modifier = Modifier.width(ManiculeSize.iconSm)) },
                )
                ManiculeStatTile(
                    value = pluralStringResource(R.plurals.home_pages, data.todayPages, data.todayPages),
                    label = stringResource(R.string.home_today_pages),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ReadingBooks(
    books: List<BookEntry>,
    onBookSelected: (String) -> Unit,
    onShowReadingBooks: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
        ManiculeSectionHeader(
            title = pluralStringResource(R.plurals.home_reading_books, books.size, books.size),
            action = ManiculeSectionHeaderAction(stringResource(R.string.home_more), onShowReadingBooks),
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
            items(books, key = { it.book.isbn }, contentType = { "home_reading_book" }) { entry ->
                ReadingBookCard(entry, onBookSelected)
            }
        }
    }
}

@Composable
private fun ReadingBookCard(
    entry: BookEntry,
    onBookSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier.width(ManiculeSize.coverMediumWidth).clickable { onBookSelected(entry.book.isbn) },
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        BookCover(entry.book.coverUrl, contentDescription = null, size = BookCoverSize.Medium, showBorder = true)
        Text(entry.book.title, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelLarge)
        val currentPage = entry.currentPage
        val totalPages = entry.book.totalPages
        if (currentPage != null && totalPages != null && totalPages > 0) {
            BookProgressBar(currentPage, totalPages)
        } else {
            Text(
                stringResource(if (currentPage == null) R.string.home_no_progress else R.string.home_no_total_pages),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoReadingBooks(
    wantBookCount: Int,
    onSearch: () -> Unit,
    onScan: (() -> Unit)?,
    onChooseWantBook: () -> Unit,
) {
    ManiculeEmptyState(
        title = stringResource(R.string.home_no_reading_title),
        description =
            if (wantBookCount > 0) {
                pluralStringResource(R.plurals.home_want_books_description, wantBookCount, wantBookCount)
            } else {
                stringResource(R.string.home_no_reading_description)
            },
        actions = {
            if (wantBookCount > 0) ManiculeButton(onClick = onChooseWantBook, text = stringResource(R.string.home_choose))
            ManiculeOutlinedButton(onClick = onSearch, text = stringResource(R.string.home_search))
            ManiculeOutlinedButton(onClick = onScan ?: {}, enabled = onScan != null, text = stringResource(R.string.home_scan))
        },
    )
}

@Composable
private fun HomeError(
    padding: androidx.compose.foundation.layout.PaddingValues,
    onRetry: () -> Unit,
) {
    ManiculeEmptyState(
        title = stringResource(R.string.home_error_title),
        description = stringResource(R.string.home_error_description),
        modifier = Modifier.fillMaxSize().padding(padding).padding(ManiculeSpacing.screenContent),
        actions = { ManiculeButton(onClick = onRetry, text = stringResource(R.string.home_retry)) },
    )
}

@ManiculePreview
@Preview(name = "Narrow", widthDp = 320, heightDp = 800, showBackground = true)
@Preview(name = "Tablet", widthDp = 900, heightDp = 700, showBackground = true)
@Composable
private fun HomeFirstUserPreview() {
    ManiculePreviewTheme {
        HomeScreen(HomeUiState.Content(previewHomeData()), {}, {}, {}, {}, {}, {}, {})
    }
}

@ManiculePreview
@Composable
private fun HomeReadingPreview() {
    ManiculePreviewTheme {
        HomeScreen(HomeUiState.Content(previewHomeData(reading = listOf(previewEntry()))), {}, {}, {}, {}, {}, {}, {})
    }
}

@ManiculePreview
@Composable
private fun HomeNoReadingPreview() {
    ManiculePreviewTheme {
        HomeScreen(HomeUiState.Content(previewHomeData(hasLibrary = true, wants = 2)), {}, {}, {}, {}, {}, {}, {})
    }
}

private fun previewHomeData(
    hasLibrary: Boolean = false,
    reading: List<BookEntry> = emptyList(),
    wants: Int = 0,
): HomeData {
    val today = LocalDate(2026, 9, 21)
    return HomeData(
        hasLibrary,
        false,
        reading,
        wants,
        today,
        42,
        3,
        (0..6).map { ReadingCalendarDay.of(today, it * 10) },
    )
}

private fun previewEntry(): BookEntry =
    BookEntry(
        book = Book(
            isbn = "9780000000001",
            title = "읽고 있는 책",
            author = "저자",
            publisher = "출판사",
            publishedDate = null,
            coverUrl = null,
            totalPages = 300,
            price = null,
            category = null,
            tableOfContentsUrl = null,
            introductionUrl = null,
            summaryUrl = null,
        ),
        status = ReadingStatus.READING,
        addedAt = Instant.DISTANT_PAST,
        updatedAt = Instant.DISTANT_PAST,
        currentPage = 120,
    )
