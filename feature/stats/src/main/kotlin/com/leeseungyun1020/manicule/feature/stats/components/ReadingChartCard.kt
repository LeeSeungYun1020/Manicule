package com.leeseungyun1020.manicule.feature.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeErrorState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartBucket
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartUnit
import com.leeseungyun1020.manicule.feature.stats.ChartKey
import com.leeseungyun1020.manicule.feature.stats.ChartState
import com.leeseungyun1020.manicule.feature.stats.R
import com.leeseungyun1020.manicule.feature.stats.StatsPeriod
import kotlinx.datetime.LocalDate

@Composable
fun ReadingChartCard(
    state: ChartState,
    unit: ReadingChartUnit,
    onUnitSelected: (ReadingChartUnit) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state == ChartState.Hidden) return
    ManiculeCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            ReadingChartHeader(unit = unit, onUnitSelected = onUnitSelected)
            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
                LegendMark(bar = true, label = stringResource(R.string.stats_chart_books_legend))
                LegendMark(bar = false, label = stringResource(R.string.stats_chart_pages_legend))
            }
            when (state) {
                is ChartState.Loading -> ManiculeLoading(Modifier.fillMaxWidth().height(ManiculeSize.chartHeight))
                is ChartState.Error -> ManiculeErrorState(
                    title = stringResource(R.string.stats_chart_error),
                    icon = ManiculeIcons.NetworkError,
                    onRetry = onRetry,
                    modifier = Modifier.fillMaxWidth(),
                )
                is ChartState.Content -> {
                    ReadingChart(buckets = state.buckets, key = state.key)
                    if (state.buckets.all { it.bookCount == 0 && it.pagesRead == 0L }) {
                        Text(
                            stringResource(R.string.stats_empty_period),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ChartState.Hidden -> Unit
            }
            Text(
                stringResource(R.string.stats_chart_explanation),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReadingChartHeader(
    unit: ReadingChartUnit,
    onUnitSelected: (ReadingChartUnit) -> Unit,
) {
    val gap = MaterialTheme.spacing.sm
    Layout(
        content = {
            Text(
                stringResource(R.string.stats_chart_title),
                modifier = Modifier.testTag("reading_chart_title"),
                style = MaterialTheme.typography.titleMedium,
            )
            ReadingChartUnitSelector(selectedUnit = unit, onUnitSelected = onUnitSelected)
        },
        modifier = Modifier.fillMaxWidth(),
    ) { measurables, constraints ->
        val selector = measurables[1].measure(constraints.copy(minWidth = 0, minHeight = 0))
        val title = measurables[0].measure(constraints.copy(minWidth = 0, minHeight = 0))
        val gapPx = gap.roundToPx()
        val sameRow = title.width + gapPx + selector.width <= constraints.maxWidth
        val height = if (sameRow) maxOf(title.height, selector.height) else title.height + gapPx + selector.height
        layout(constraints.maxWidth, height) {
            title.placeRelative(0, if (sameRow) (height - title.height) / 2 else 0)
            selector.placeRelative(
                constraints.maxWidth - selector.width,
                if (sameRow) (height - selector.height) / 2 else title.height + gapPx,
            )
        }
    }
}

@Composable
private fun LegendMark(
    bar: Boolean,
    label: String,
) {
    val markColor = if (bar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
        Canvas(Modifier.size(ManiculeSize.iconXs)) {
            if (bar) {
                drawRect(
                    markColor,
                    topLeft = Offset(size.width * .3f, size.height * .15f),
                    size = androidx.compose.ui.geometry.Size(size.width * .4f, size.height * .7f),
                )
            } else {
                drawLine(
                    markColor,
                    Offset(0f, size.height / 2),
                    Offset(size.width, size.height / 2),
                    ManiculeSize.chartLineWidth.toPx(),
                )
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@ManiculePreview
@Composable
private fun ReadingChartCardPreview() {
    val date = LocalDate(2026, 9, 1)
    ManiculePreviewTheme {
        ReadingChartCard(
            state = ChartState.Content(
                ChartKey(StatsPeriod.CUSTOM, date, date, ReadingChartUnit.DAY),
                listOf(ReadingChartBucket(date, date, 1, 42)),
            ),
            unit = ReadingChartUnit.DAY,
            onUnitSelected = {},
            onRetry = {},
        )
    }
}
