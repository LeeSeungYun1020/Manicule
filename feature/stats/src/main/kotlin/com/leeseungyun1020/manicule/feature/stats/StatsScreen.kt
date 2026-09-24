package com.leeseungyun1020.manicule.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeErrorState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
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
fun StatsScreenRoute(viewModel: StatsViewModel = hiltViewModel()) {
    val state = viewModel.uiState.collectAsStateWithLifecycle().value
    StatsScreen(
        state = state,
        onDateSelected = viewModel::selectDate,
        onDismissDay = viewModel::dismissDay,
        onRetryPeriod = viewModel::retryPeriod,
        onRetryDay = viewModel::retryDay,
        consumeRefreshError = viewModel::consumeRefreshError,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    state: StatsUiState,
    onDateSelected: (LocalDate) -> Unit,
    onDismissDay: () -> Unit,
    onRetryPeriod: () -> Unit,
    onRetryDay: () -> Unit,
    consumeRefreshError: (Int) -> Boolean,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val period = state.period
    val refreshErrorId = (period as? PeriodState.Content)?.refreshErrorId ?: 0
    val refreshErrorText = stringResource(R.string.stats_refresh_error)
    val retryText = stringResource(R.string.stats_retry)
    LaunchedEffect(refreshErrorId) {
        if (refreshErrorId > 0 && consumeRefreshError(refreshErrorId)) {
            if (snackbarHostState.showSnackbar(refreshErrorText, retryText) == SnackbarResult.ActionPerformed) {
                onRetryPeriod()
            }
        }
    }

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
                selectedDate = when (val day = state.day) {
                    is DayState.Loading -> day.date
                    is DayState.Content -> day.date
                    is DayState.Error -> day.date
                    DayState.Closed -> null
                },
                onDateSelected = onDateSelected,
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        }
    }
    if (state.day != DayState.Closed) {
        ReadingDayBottomSheet(state.day, onDismissDay, onRetryDay)
    }
}

@Composable
private fun StatsContent(
    period: PeriodState.Content,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState()).padding(ManiculeSpacing.screenContent),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
    ) {
        Text(
            text = stringResource(R.string.stats_recent_four_weeks),
            style = MaterialTheme.typography.titleMedium,
        )
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
        StatsCalendarCard(
            days = period.days,
            today = period.today,
            selectedDate = selectedDate,
            onDateSelected = onDateSelected,
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

@Composable
private fun StatsSummary(
    summary: PeriodSummary,
    modifier: Modifier = Modifier,
) {
    val tiles = listOf(
        pluralStringResource(R.plurals.stats_days_value, summary.longestStreak, summary.longestStreak) to
            stringResource(R.string.stats_streak),
        pluralStringResource(R.plurals.stats_pages_value, summary.pagesRead, summary.pagesRead) to stringResource(R.string.stats_pages),
        pluralStringResource(R.plurals.stats_books_value, summary.bookCount, summary.bookCount) to stringResource(R.string.stats_books),
    )
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        if (maxWidth < ManiculeSize.coverMediumWidth * 3) {
            Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                tiles.forEach { (value, label) -> ManiculeStatTile(value, label, modifier = Modifier.fillMaxWidth()) }
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                tiles.forEach { (value, label) -> ManiculeStatTile(value, label, modifier = Modifier.weight(1f)) }
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
                    days = listOf(ReadingCalendarDay.of(today, 25)),
                    summary = PeriodSummary(today, today, 1, 25, 1),
                ),
            ),
            onDateSelected = {},
            onDismissDay = {},
            onRetryPeriod = {},
            onRetryDay = {},
            consumeRefreshError = { true },
        )
    }
}
