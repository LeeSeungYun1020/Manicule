package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
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
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.feature.stats.components.ReadingDayBottomSheet
import com.leeseungyun1020.manicule.feature.stats.components.StatsCalendarCard
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
        onDateSelected = viewModel::selectDate,
        onDismissDay = viewModel::dismissDay,
        onRetryPeriod = viewModel::retryPeriod,
        onRetryDay = viewModel::retryDay,
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
    onDateSelected: (LocalDate) -> Unit,
    onDismissDay: () -> Unit,
    onRetryPeriod: () -> Unit,
    onRetryDay: () -> Unit,
    onBookSelected: (String) -> Unit,
    consumeRefreshError: (Int) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val period = state.period

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
                selectedDate = state.day.selectedDate,
                onPeriodSelected = onPeriodSelected,
                onDateSelected = onDateSelected,
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

private val PeriodState.refreshErrorId: Int
    get() = (this as? PeriodState.Content)?.refreshErrorId ?: 0

private val DayState.refreshErrorId: Int
    get() = (this as? DayState.Content)?.refreshErrorId ?: 0

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
    selectedDate: LocalDate?,
    onPeriodSelected: (StatsPeriod) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(ManiculeSpacing.screenContent),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        val todayLabel = stringResource(R.string.stats_period_today)
        val fourWeeksLabel = stringResource(R.string.stats_period_four_weeks)
        val oneYearLabel = stringResource(R.string.stats_period_one_year)
        val customLabel = stringResource(R.string.stats_period_custom)
        ManiculeSegmentedButton(
            options = StatsPeriod.entries,
            selectedOption = period.selectedPeriod,
            onOptionSelected = onPeriodSelected,
            disabledOptions = setOf(StatsPeriod.CUSTOM),
            itemLabel = { option ->
                when (option) {
                    StatsPeriod.TODAY -> todayLabel
                    StatsPeriod.FOUR_WEEKS -> fourWeeksLabel
                    StatsPeriod.ONE_YEAR -> oneYearLabel
                    StatsPeriod.CUSTOM -> customLabel
                }
            },
        )
        if (period.selectedPeriod == StatsPeriod.TODAY) {
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
            )
        }
        StatsCalendarCard(
            days = period.days,
            today = period.today,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
            isTodayPeriod = period.selectedPeriod == StatsPeriod.TODAY,
        )
        StatsSummary(period.summary)
        if (period.summary.pagesRead == 0) {
            Text(
                text = stringResource(R.string.stats_empty_period),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
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
            ),
            onPeriodSelected = {},
            onDateSelected = {},
            onDismissDay = {},
            onRetryPeriod = {},
            onRetryDay = {},
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
            onDateSelected = {},
            onDismissDay = {},
            onRetryPeriod = {},
            onRetryDay = {},
            onBookSelected = {},
            consumeRefreshError = { true },
        )
    }
}
