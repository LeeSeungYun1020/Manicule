package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeErrorState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSegmentedButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSnackbarHost
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeStatTile
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartUnit
import com.leeseungyun1020.manicule.core.domain.stats.ReadingDayBook
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.feature.stats.components.CustomPeriodBottomSheet
import com.leeseungyun1020.manicule.feature.stats.components.ReadingChartCard
import com.leeseungyun1020.manicule.feature.stats.components.ReadingDayBookItem
import com.leeseungyun1020.manicule.feature.stats.components.ReadingDayBottomSheet
import com.leeseungyun1020.manicule.feature.stats.components.StatsCalendarCard
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

@Composable
fun StatsScreenRoute(
    onBookSelected: (String) -> Unit,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    StatsScreen(
        state = state,
        onPeriodSelected = viewModel::selectPeriod,
        onApplyCustomPeriod = viewModel::applyCustomPeriod,
        onDateSelected = viewModel::selectDate,
        onDismissDay = viewModel::dismissDay,
        onRetryPeriod = viewModel::retryPeriod,
        onRetryDay = viewModel::retryDay,
        onRetryTodayBooks = viewModel::retryTodayBooks,
        onChartUnitSelected = viewModel::selectChartUnit,
        onRetryChart = viewModel::retryChart,
        onBookSelected = { isbn ->
            viewModel.dismissDay()
            onBookSelected(isbn)
        },
        consumeRefreshError = viewModel::consumeRefreshError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    state: StatsUiState,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onApplyCustomPeriod: (LocalDate, LocalDate) -> Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onDismissDay: () -> Unit,
    onRetryPeriod: () -> Unit,
    onRetryDay: () -> Unit,
    onRetryTodayBooks: () -> Unit,
    onChartUnitSelected: (ReadingChartUnit) -> Unit,
    onRetryChart: () -> Unit,
    onBookSelected: (String) -> Unit,
    consumeRefreshError: (Int) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val period = state.period
    var showCustomPeriodSheet by rememberSaveable { mutableStateOf(false) }

    HandleRefreshError(
        errorId = period.refreshErrorId,
        consumeRefreshError = consumeRefreshError,
        snackbarHostState = snackbarHostState,
        onRetry = onRetryPeriod,
    )
    HandleRefreshError(
        errorId = state.day.refreshErrorId,
        consumeRefreshError = consumeRefreshError,
        snackbarHostState = snackbarHostState,
        onRetry = onRetryDay,
    )
    HandleRefreshError(
        errorId = state.todayBooks.refreshErrorId,
        consumeRefreshError = consumeRefreshError,
        snackbarHostState = snackbarHostState,
        onRetry = onRetryTodayBooks,
    )
    HandleRefreshError(
        errorId = (state.chart as? ChartState.Content)?.refreshErrorId ?: 0,
        consumeRefreshError = consumeRefreshError,
        snackbarHostState = snackbarHostState,
        onRetry = onRetryChart,
    )

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ManiculeTopAppBar(
                title = stringResource(R.string.stats_title),
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { ManiculeSnackbarHost(snackbarHostState) },
    ) { padding ->
        when (period) {
            PeriodState.Loading -> ManiculeLoading(Modifier.fillMaxSize().padding(padding))
            PeriodState.Error -> ManiculeErrorState(
                title = stringResource(R.string.stats_error_title),
                description = stringResource(R.string.stats_error_description),
                icon = ManiculeIcons.NetworkError,
                onRetry = onRetryPeriod,
                modifier = Modifier.fillMaxSize().padding(padding).padding(ManiculeSpacing.screenContent),
            )
            is PeriodState.Content -> StatsContent(
                period = period,
                todayBooks = state.todayBooks,
                selectedDate = state.day.selectedDate,
                onPeriodSelected = onPeriodSelected,
                onOpenCustomPeriodSheet = { showCustomPeriodSheet = true },
                onDateSelected = onDateSelected,
                onRetryTodayBooks = onRetryTodayBooks,
                chart = state.chart,
                onChartUnitSelected = onChartUnitSelected,
                onRetryChart = onRetryChart,
                onBookSelected = onBookSelected,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
    if (state.day != DayState.Closed) {
        ReadingDayBottomSheet(
            state = state.day,
            onDismiss = onDismissDay,
            onRetry = onRetryDay,
            onBookSelected = onBookSelected,
            snackbarHostState = snackbarHostState,
        )
    }
    if (showCustomPeriodSheet && period is PeriodState.Content) {
        val initialRange = period.customRange ?: CustomPeriodRange.defaultFor(period.today)
        CustomPeriodBottomSheet(
            today = period.today,
            initialRange = initialRange,
            onApply = { start, end ->
                if (onApplyCustomPeriod(start, end)) {
                    showCustomPeriodSheet = false
                }
            },
            onDismiss = { showCustomPeriodSheet = false },
        )
    }
}

@Composable
private fun HandleRefreshError(
    errorId: Int,
    consumeRefreshError: (Int) -> Boolean,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
) {
    val refreshErrorText = stringResource(R.string.stats_refresh_error)
    val retryText = stringResource(R.string.stats_retry)
    LaunchedEffect(errorId) {
        if (errorId > 0 && consumeRefreshError(errorId)) {
            if (snackbarHostState.showSnackbar(refreshErrorText, retryText) == SnackbarResult.ActionPerformed) {
                onRetry()
            }
        }
    }
}

private const val TODAY_BOOKS_HEADER_INDEX = 4

private val PeriodState.refreshErrorId: Int
    get() = (this as? PeriodState.Content)?.refreshErrorId ?: 0

private val DayState.refreshErrorId: Int
    get() = (this as? DayState.Content)?.refreshErrorId ?: 0

private val TodayBooksState.refreshErrorId: Int
    get() = (this as? TodayBooksState.Content)?.refreshErrorId ?: 0

private val DayState.selectedDate: LocalDate?
    get() = when (this) {
        is DayState.Loading -> date
        is DayState.Content -> date
        is DayState.Error -> date
        DayState.Closed -> null
    }

@Composable
private fun StatsContent(
    period: PeriodState.Content,
    todayBooks: TodayBooksState,
    selectedDate: LocalDate?,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onOpenCustomPeriodSheet: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onRetryTodayBooks: () -> Unit,
    chart: ChartState,
    onChartUnitSelected: (ReadingChartUnit) -> Unit,
    onRetryChart: () -> Unit,
    onBookSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isTodayPeriod = period.selectedPeriod == StatsPeriod.TODAY
    val todayLabel = stringResource(R.string.stats_period_today)
    val fourWeeksLabel = stringResource(R.string.stats_period_four_weeks)
    val oneYearLabel = stringResource(R.string.stats_period_one_year)
    val customLabel = stringResource(R.string.stats_period_custom)

    LazyColumn(
        state = listState,
        contentPadding = ManiculeSpacing.screenContent,
        modifier = modifier,
    ) {
        item(key = "period_segmented_button", contentType = "period_segmented_button") {
            ManiculeSegmentedButton(
                options = StatsPeriod.entries,
                selectedOption = period.selectedPeriod,
                onOptionSelected = { option ->
                    if (option == StatsPeriod.CUSTOM) {
                        onOpenCustomPeriodSheet()
                    } else {
                        onPeriodSelected(option)
                    }
                },
                disabledOptions = emptySet(),
                itemLabel = statsPeriodLabel(
                    todayLabel = todayLabel,
                    fourWeeksLabel = fourWeeksLabel,
                    oneYearLabel = oneYearLabel,
                    customLabel = customLabel,
                ),
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        }

        item(key = "date_label", contentType = "date_label") {
            StatsDateLabel(
                period = period,
                isTodayPeriod = isTodayPeriod,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        }

        item(key = "calendar_card", contentType = "calendar_card") {
            StatsCalendarCard(
                days = period.days,
                today = period.today,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected,
                isTodayPeriod = isTodayPeriod,
                onTodayClicked = if (isTodayPeriod) {
                    {
                        coroutineScope.launch {
                            listState.animateScrollToItem(TODAY_BOOKS_HEADER_INDEX)
                        }
                    }
                } else {
                    null
                },
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        }

        item(key = "summary", contentType = "summary") {
            StatsSummary(period.summary)
        }

        if (!isTodayPeriod) {
            item(key = "reading_chart", contentType = "reading_chart") {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
                val unit = when (chart) {
                    is ChartState.Loading -> chart.key.unit
                    is ChartState.Error -> chart.key.unit
                    is ChartState.Content -> chart.key.unit
                    ChartState.Hidden -> defaultChartUnit(
                        period.selectedPeriod,
                        period.summary.rangeStart,
                        period.summary.rangeEnd,
                    )
                }
                ReadingChartCard(chart, unit, onChartUnitSelected, onRetryChart)
            }
        }

        if (isTodayPeriod) {
            todayBooksSection(
                todayBooks = todayBooks,
                onRetryTodayBooks = onRetryTodayBooks,
                onBookSelected = onBookSelected,
            )
        }
    }
}

private fun statsPeriodLabel(
    todayLabel: String,
    fourWeeksLabel: String,
    oneYearLabel: String,
    customLabel: String,
): (StatsPeriod) -> String =
    { option ->
        when (option) {
            StatsPeriod.TODAY -> todayLabel
            StatsPeriod.FOUR_WEEKS -> fourWeeksLabel
            StatsPeriod.ONE_YEAR -> oneYearLabel
            StatsPeriod.CUSTOM -> customLabel
        }
    }

@Composable
private fun StatsDateLabel(
    period: PeriodState.Content,
    isTodayPeriod: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isTodayPeriod) {
        val today = period.today
        Text(
            text = stringResource(
                R.string.stats_single_date,
                today.year,
                today.monthNumber,
                today.dayOfMonth,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
    } else {
        val start = period.summary.rangeStart
        val end = period.summary.rangeEnd
        Text(
            text = stringResource(
                R.string.stats_date_range,
                start.year,
                start.monthNumber,
                start.dayOfMonth,
                end.year,
                end.monthNumber,
                end.dayOfMonth,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier,
        )
    }
}

private fun LazyListScope.todayBooksHeader(todayBooks: TodayBooksState) {
    item(key = "today_books_header", contentType = "today_books_header") {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
        val headerText = when (todayBooks) {
            is TodayBooksState.Content -> pluralStringResource(
                R.plurals.stats_today_books_title,
                todayBooks.rows.size,
                todayBooks.rows.size,
            )
            else -> stringResource(R.string.stats_today_books_section_title)
        }
        Text(
            text = headerText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier
                .fillMaxWidth()
                .semantics { heading() },
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
    }
}

private fun LazyListScope.todayBooksContent(
    todayBooks: TodayBooksState,
    onRetryTodayBooks: () -> Unit,
    onBookSelected: (String) -> Unit,
) {
    when (todayBooks) {
        TodayBooksState.Hidden -> Unit
        TodayBooksState.Loading -> {
            item(key = "today_books_loading", contentType = "today_books_loading") {
                ManiculeLoading(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.xl),
                )
            }
        }
        TodayBooksState.Error -> {
            item(key = "today_books_error", contentType = "today_books_error") {
                ManiculeErrorState(
                    title = stringResource(R.string.stats_today_books_error),
                    icon = ManiculeIcons.NetworkError,
                    onRetry = onRetryTodayBooks,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.lg),
                )
            }
        }
        is TodayBooksState.Content -> {
            if (todayBooks.rows.isEmpty()) {
                item(key = "today_books_empty", contentType = "today_books_empty") {
                    Text(
                        text = stringResource(R.string.stats_today_books_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = MaterialTheme.spacing.sm),
                    )
                }
            } else {
                items(
                    items = todayBooks.rows,
                    key = { it.isbn },
                    contentType = { "today-book-item" },
                ) { row ->
                    ReadingDayBookItem(
                        book = row,
                        onBookSelected = onBookSelected,
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun LazyListScope.todayBooksSection(
    todayBooks: TodayBooksState,
    onRetryTodayBooks: () -> Unit,
    onBookSelected: (String) -> Unit,
) {
    todayBooksHeader(todayBooks)
    todayBooksContent(
        todayBooks = todayBooks,
        onRetryTodayBooks = onRetryTodayBooks,
        onBookSelected = onBookSelected,
    )
}

private data class StatTileItem(
    val value: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
private fun StatsSummary(
    summary: PeriodSummary,
    modifier: Modifier = Modifier,
) {
    val tiles = listOf(
        StatTileItem(
            value = pluralStringResource(R.plurals.stats_days_value, summary.longestStreak, summary.longestStreak),
            label = stringResource(R.string.stats_streak),
            icon = ManiculeIcons.Streak,
        ),
        StatTileItem(
            value = pluralStringResource(R.plurals.stats_pages_value, summary.pagesRead, summary.pagesRead),
            label = stringResource(R.string.stats_pages),
            icon = ManiculeIcons.Pages,
        ),
        StatTileItem(
            value = pluralStringResource(R.plurals.stats_books_value, summary.bookCount, summary.bookCount),
            label = stringResource(R.string.stats_books),
            icon = ManiculeIcons.Book,
        ),
    )
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < ManiculeSize.coverMediumWidth * 3) {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                tiles.forEach { tile ->
                    ManiculeStatTile(
                        value = tile.value,
                        label = tile.label,
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                imageVector = tile.icon,
                                contentDescription = null,
                                modifier = Modifier.size(ManiculeSize.iconSm),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                tiles.forEach { tile ->
                    ManiculeStatTile(
                        value = tile.value,
                        label = tile.label,
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                imageVector = tile.icon,
                                contentDescription = null,
                                modifier = Modifier.size(ManiculeSize.iconSm),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                    )
                }
            }
        }
    }
}

@ManiculePreview
@Composable
private fun StatsScreenPreview() {
    val today = LocalDate(2026, 9, 24)
    ManiculePreviewTheme {
        StatsScreen(
            state = StatsUiState(
                period = PeriodState.Content(
                    today = today,
                    selectedPeriod = StatsPeriod.TODAY,
                    days = listOf(ReadingCalendarDay.of(today, 25)),
                    summary = PeriodSummary(today, today, 1, 25, 1),
                ),
                todayBooks = TodayBooksState.Content(
                    date = today,
                    rows = listOf(ReadingDayBook("isbn-1", null, 1, 25)),
                ),
            ),
            onPeriodSelected = {},
            onApplyCustomPeriod = { _, _ -> true },
            onDateSelected = {},
            onDismissDay = {},
            onRetryPeriod = {},
            onRetryDay = {},
            onRetryTodayBooks = {},
            onChartUnitSelected = {},
            onRetryChart = {},
            onBookSelected = {},
            consumeRefreshError = { true },
        )
    }
}

@ManiculePreview
@Composable
private fun StatsScreenTodayEmptyPreview() {
    val today = LocalDate(2026, 9, 24)
    ManiculePreviewTheme {
        StatsScreen(
            state = StatsUiState(
                period = PeriodState.Content(
                    today = today,
                    selectedPeriod = StatsPeriod.TODAY,
                    days = listOf(ReadingCalendarDay.of(today, 0)),
                    summary = PeriodSummary(today, today, 0, 0, 0),
                ),
                todayBooks = TodayBooksState.Content(
                    date = today,
                    rows = emptyList(),
                ),
            ),
            onPeriodSelected = {},
            onApplyCustomPeriod = { _, _ -> true },
            onDateSelected = {},
            onDismissDay = {},
            onRetryPeriod = {},
            onRetryDay = {},
            onRetryTodayBooks = {},
            onChartUnitSelected = {},
            onRetryChart = {},
            onBookSelected = {},
            consumeRefreshError = { true },
        )
    }
}

@ManiculePreview
@Composable
private fun StatsScreenFourWeeksPreview() {
    val today = LocalDate(2026, 9, 24)
    val start = LocalDate(2026, 8, 28)
    ManiculePreviewTheme {
        StatsScreen(
            state = StatsUiState(
                period = PeriodState.Content(
                    today = today,
                    selectedPeriod = StatsPeriod.FOUR_WEEKS,
                    days = listOf(ReadingCalendarDay.of(today, 25)),
                    summary = PeriodSummary(start, today, 5, 250, 3),
                ),
            ),
            onPeriodSelected = {},
            onApplyCustomPeriod = { _, _ -> true },
            onDateSelected = {},
            onDismissDay = {},
            onRetryPeriod = {},
            onRetryDay = {},
            onRetryTodayBooks = {},
            onChartUnitSelected = {},
            onRetryChart = {},
            onBookSelected = {},
            consumeRefreshError = { true },
        )
    }
}

@ManiculePreview
@Composable
private fun StatsScreenCustomPeriodPreview() {
    val today = LocalDate(2026, 9, 24)
    val start = LocalDate(2026, 6, 12)
    val end = LocalDate(2026, 7, 9)
    ManiculePreviewTheme {
        StatsScreen(
            state = StatsUiState(
                period = PeriodState.Content(
                    today = today,
                    selectedPeriod = StatsPeriod.CUSTOM,
                    customRange = CustomPeriodRange(start, end),
                    days = listOf(ReadingCalendarDay.of(today, 25)),
                    summary = PeriodSummary(start, end, 3, 150, 2),
                ),
            ),
            onPeriodSelected = {},
            onApplyCustomPeriod = { _, _ -> true },
            onDateSelected = {},
            onDismissDay = {},
            onRetryPeriod = {},
            onRetryDay = {},
            onRetryTodayBooks = {},
            onChartUnitSelected = {},
            onRetryChart = {},
            onBookSelected = {},
            consumeRefreshError = { true },
        )
    }
}
