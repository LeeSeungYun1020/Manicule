@file:Suppress("TooManyFunctions")

package com.leeseungyun1020.manicule.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeDashedCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeOutlinedButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeader
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeaderAction
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.domain.home.HomeData
import com.leeseungyun1020.manicule.core.domain.home.HomeReadingSummary
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.ui.book.BookCover
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize
import com.leeseungyun1020.manicule.core.ui.book.BookProgressBar
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarCell
import com.leeseungyun1020.manicule.core.ui.calendar.ReadingCalendarLegend
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import com.leeseungyun1020.manicule.core.ui.R as CoreUiR

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onBookSelected: (String) -> Unit,
    onShowReadingBooks: () -> Unit,
    onChooseWantBook: () -> Unit,
    onShowStats: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { HomeSearchTopBar(onSearch = onSearch, onScan = onScan) },
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
                    onRetry = onRetry,
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
    onScan: () -> Unit,
    onBookSelected: (String) -> Unit,
    onShowReadingBooks: () -> Unit,
    onChooseWantBook: () -> Unit,
    onShowStats: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier,
) {
    val paddedContentModifier = Modifier.fillMaxWidth().padding(horizontal = ManiculeSpacing.screenHorizontal)
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = ManiculeSize.contentMaxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(top = ManiculeSpacing.sm, bottom = ManiculeSpacing.xxl),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl),
        ) {
            if (isFirstUser) {
                OnboardingContent(onSearch, onScan, modifier = paddedContentModifier)
            } else {
                ReadingSummary(data.summary, onShowStats, onRetry, modifier = paddedContentModifier)
                if (data.readingBooks.isEmpty()) {
                    NoReadingBooks(data.wantBookCount, onSearch, onScan, onChooseWantBook, modifier = paddedContentModifier)
                } else {
                    ReadingBooks(data.readingBooks, onBookSelected, onShowReadingBooks)
                }
            }
        }
    }
}

@Composable
private fun OnboardingContent(
    onSearch: () -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xl)) {
        ManiculeDashedCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                SummaryMetrics(streak = 0, pages = 0, enabled = false)
                HomeWeekStrip(days = null)
                Text(
                    stringResource(R.string.home_empty_summary_description),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
        ManiculeCard {
            Column(
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
            ) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.home_onboarding_heading), style = MaterialTheme.typography.titleMedium)
                    Text(
                        stringResource(R.string.home_onboarding_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OnboardingStep(
                    icon = ManiculeIcons.Search,
                    title = stringResource(R.string.home_onboarding_step_one),
                    description = stringResource(R.string.home_onboarding_step_one_description),
                )
                OnboardingStep(
                    icon = ManiculeIcons.Edit,
                    title = stringResource(R.string.home_onboarding_step_two),
                    description = stringResource(R.string.home_onboarding_step_two_description),
                )
                OnboardingStep(
                    icon = ManiculeIcons.Tab.StatsFilled,
                    title = stringResource(R.string.home_onboarding_step_three),
                    description = stringResource(R.string.home_onboarding_step_three_description),
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    ManiculeButton(
                        onClick = onSearch,
                        text = stringResource(R.string.home_search),
                        leadingIcon = { Icon(ManiculeIcons.Search, contentDescription = null) },
                    )
                    ManiculeOutlinedButton(
                        onClick = onScan,
                        text = stringResource(R.string.home_scan),
                        leadingIcon = { Icon(ManiculeIcons.ScanBarcode, contentDescription = null) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingStep(
    icon: ImageVector,
    title: String,
    description: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md), verticalAlignment = Alignment.Top) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Box(
                modifier = Modifier.size(ManiculeSize.iconLg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(ManiculeSize.iconXs))
            }
        }
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ReadingSummary(
    summary: HomeReadingSummary?,
    onShowStats: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (summary == null) {
        ManiculeEmptyState(
            title = stringResource(R.string.home_summary_error_title),
            modifier = modifier,
            description = stringResource(R.string.home_summary_error_description),
            actions = { ManiculeButton(onClick = onRetry, text = stringResource(R.string.home_retry)) },
        )
        return
    }
    ManiculeCard(modifier = modifier.clickable(onClick = onShowStats)) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            SummaryMetrics(streak = summary.currentStreak, pages = summary.todayPages)
            HomeWeekStrip(days = summary.recentDays)
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                ReadingCalendarLegend()
            }
        }
    }
}

@Composable
private fun SummaryMetrics(
    streak: Int,
    pages: Int,
    enabled: Boolean = true,
) {
    val color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg), verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, modifier = Modifier.size(ManiculeSize.iconXs), tint = color)
            Text(stringResource(R.string.home_streak), style = MaterialTheme.typography.labelMedium, color = color)
            Text(pluralStringResource(R.plurals.home_days, streak, streak), style = MaterialTheme.typography.titleSmall, color = color)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.home_today_pages), style = MaterialTheme.typography.labelMedium, color = color)
            Text(pluralStringResource(R.plurals.home_pages, pages, pages), style = MaterialTheme.typography.titleSmall, color = color)
        }
    }
}

@Composable
private fun HomeWeekStrip(days: List<ReadingCalendarDay>?) {
    val weekdays = stringArrayResource(R.array.home_weekdays)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        repeat(7) { index ->
            val day = days?.getOrNull(index)
            val dayDescription =
                day?.let {
                    if (it.pages == 0) {
                        stringResource(
                            CoreUiR.string.reading_calendar_cell_no_record_content_description,
                            it.date.year,
                            it.date.monthNumber,
                            it.date.dayOfMonth,
                        )
                    } else {
                        pluralStringResource(
                            CoreUiR.plurals.reading_calendar_cell_content_description,
                            it.pages,
                            it.date.year,
                            it.date.monthNumber,
                            it.date.dayOfMonth,
                            it.pages,
                        )
                    }
                }
            Column(
                modifier =
                    Modifier.weight(1f).then(
                        if (dayDescription == null) {
                            Modifier
                        } else {
                            Modifier.semantics(mergeDescendants = true) {
                                contentDescription = dayDescription
                            }
                        },
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ReadingCalendarCell(
                    intensity = day?.intensity ?: 0,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                )
                if (day != null) {
                    Text(
                        weekdays[day.date.dayOfWeek.value - 1],
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
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
            modifier = Modifier.padding(horizontal = ManiculeSpacing.screenHorizontal),
            action = ManiculeSectionHeaderAction(stringResource(R.string.home_more), onShowReadingBooks),
        )
        LazyRow(
            modifier = Modifier.fillMaxWidth().testTag("home_reading_books"),
            contentPadding = PaddingValues(horizontal = ManiculeSpacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
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
            BookProgressBar(currentPage, totalPages, showDetails = false)
        }
    }
}

@Composable
private fun NoReadingBooks(
    wantBookCount: Int,
    onSearch: () -> Unit,
    onScan: () -> Unit,
    onChooseWantBook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManiculeEmptyState(
        title = stringResource(R.string.home_no_reading_title),
        modifier = modifier,
        description =
            if (wantBookCount > 0) {
                pluralStringResource(R.plurals.home_want_books_description, wantBookCount, wantBookCount)
            } else {
                stringResource(R.string.home_no_reading_description)
            },
        icon = {
            Icon(
                imageVector = ManiculeIcons.Tab.LibraryOutlined,
                contentDescription = null,
                modifier = Modifier.size(ManiculeSize.iconLg),
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        actions = {
            if (wantBookCount > 0) {
                ManiculeButton(
                    onClick = onChooseWantBook,
                    text = stringResource(R.string.home_choose),
                    leadingIcon = { Icon(ManiculeIcons.Bookmark, contentDescription = null) },
                )
                OutlinedIconButton(onClick = onSearch, modifier = Modifier.size(ManiculeSize.touchTargetMin)) {
                    Icon(ManiculeIcons.Search, contentDescription = stringResource(R.string.home_search))
                }
                OutlinedIconButton(onClick = onScan, modifier = Modifier.size(ManiculeSize.touchTargetMin)) {
                    Icon(ManiculeIcons.ScanBarcode, contentDescription = stringResource(R.string.home_scan))
                }
            } else {
                ManiculeButton(
                    onClick = onSearch,
                    text = stringResource(R.string.home_search),
                    leadingIcon = { Icon(ManiculeIcons.Search, contentDescription = null) },
                )
                ManiculeOutlinedButton(
                    onClick = onScan,
                    text = stringResource(R.string.home_scan),
                    leadingIcon = { Icon(ManiculeIcons.ScanBarcode, contentDescription = null) },
                )
            }
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
@Preview(name = "Tablet", widthDp = 900, heightDp = 700, showBackground = true)
@Composable
private fun HomeReadingPreview() {
    ManiculePreviewTheme {
        HomeScreen(HomeUiState.Content(previewHomeData(hasLibrary = true, reading = listOf(previewEntry()))), {}, {}, {}, {}, {}, {}, {})
    }
}

@ManiculePreview
@Composable
private fun HomeNoReadingWantPreview() {
    ManiculePreviewTheme {
        HomeScreen(HomeUiState.Content(previewHomeData(hasLibrary = true, wants = 2)), {}, {}, {}, {}, {}, {}, {})
    }
}

@ManiculePreview
@Composable
private fun HomeNoReadingEmptyPreview() {
    ManiculePreviewTheme {
        HomeScreen(HomeUiState.Content(previewHomeData(hasLibrary = true, wants = 0)), {}, {}, {}, {}, {}, {}, {})
    }
}

@ManiculePreview
@Composable
private fun HomeLoadingPreview() {
    ManiculePreviewTheme {
        HomeScreen(HomeUiState.Loading, {}, {}, {}, {}, {}, {}, {})
    }
}

@ManiculePreview
@Composable
private fun HomeErrorPreview() {
    ManiculePreviewTheme {
        HomeScreen(HomeUiState.Error, {}, {}, {}, {}, {}, {}, {})
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
        HomeReadingSummary(
            today = today,
            todayPages = 42,
            currentStreak = 3,
            recentDays = (0..6).map { index -> ReadingCalendarDay.of(today.minus(DatePeriod(days = 6 - index)), index * 10) },
        ),
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
