package com.leeseungyun1020.manicule.feature.stats.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeBorder
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartBucket
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartUnit
import com.leeseungyun1020.manicule.feature.stats.ChartKey
import com.leeseungyun1020.manicule.feature.stats.R
import com.leeseungyun1020.manicule.feature.stats.StatsPeriod
import kotlinx.datetime.LocalDate
import java.math.BigInteger
import kotlin.math.ceil
import kotlin.math.max

internal fun chartTickStep(maximum: Long): Long {
    val needed = max(1L, maximum / 4 + if (maximum % 4 == 0L) 0 else 1)
    var power = 1L
    while (power <= Long.MAX_VALUE / 10 && power * 10 < needed) power *= 10
    for (factor in longArrayOf(1, 2, 5, 10)) {
        if (power <= Long.MAX_VALUE / factor && power * factor >= needed) return power * factor
    }
    return power
}

private fun tickLabel(
    step: Long,
    tick: Int,
): String = BigInteger.valueOf(step).multiply(BigInteger.valueOf(tick.toLong())).toString()

private data class ChartMetrics(
    val bookStep: Long,
    val pageStep: Long,
    val leftWidth: Dp,
    val rightWidth: Dp,
    val middleWidth: Dp,
    val slotWidth: Dp,
    val chartHeight: Dp,
    val plotInset: Dp,
    val tickLabelHeight: Dp,
    val labelStyle: TextStyle,
    val direction: LayoutDirection,
    val onXLabelMeasured: (Int) -> Unit,
    val onBookTickMeasured: (Int, Int) -> Unit,
    val onPageTickMeasured: (Int, Int) -> Unit,
)

private data class ChartColors(
    val axis: Color,
    val bar: Color,
    val line: Color,
    val grid: Color,
    val halo: Color,
)

@Composable
fun ReadingChart(
    buckets: List<ReadingChartBucket>,
    key: ChartKey,
    modifier: Modifier = Modifier,
) {
    if (buckets.isEmpty()) return
    val bookStep = remember(buckets) { chartTickStep(buckets.maxOf { it.bookCount.toLong() }) }
    val pageStep = remember(buckets) { chartTickStep(buckets.maxOf { it.pagesRead }) }
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labelStyle = MaterialTheme.typography.labelSmall
    var observedXWidth by remember(key) { mutableIntStateOf(0) }
    var observedBookWidth by remember(key) { mutableIntStateOf(0) }
    var observedPageWidth by remember(key) { mutableIntStateOf(0) }
    var observedTickHeight by remember(key) { mutableIntStateOf(0) }
    val tickLabels = (0..4).flatMap { tick -> listOf(tickLabel(bookStep, tick), tickLabel(pageStep, tick)) }
    val labelHeight = with(density) {
        maxOf(observedTickHeight, tickLabels.maxOf { textMeasurer.measure(it, labelStyle).size.height }).toDp()
    }
    val plotInset = maxOf(ManiculeSpacing.sm, labelHeight / 2)
    val chartHeight = maxOf(CHART_MIN_HEIGHT, labelHeight * 5 + ManiculeSpacing.sm * 4)
    val leftWidth = with(density) {
        maxOf(observedBookWidth, (0..4).maxOf { textMeasurer.measure(tickLabel(bookStep, it), labelStyle).size.width })
            .toDp() + ManiculeSpacing.sm
    }
    val rightWidth = with(density) {
        maxOf(observedPageWidth, (0..4).maxOf { textMeasurer.measure(tickLabel(pageStep, it), labelStyle).size.width })
            .toDp() + ManiculeSpacing.sm
    }
    val widestXLabel = with(density) {
        maxOf(observedXWidth, buckets.maxOf { textMeasurer.measure(chartLabel(it, key.unit), labelStyle).size.width })
            .toDp()
    }
    val colors = ChartColors(
        axis = MaterialTheme.colorScheme.onSurfaceVariant,
        bar = MaterialTheme.colorScheme.primary,
        line = MaterialTheme.colorScheme.onSurface,
        grid = MaterialTheme.colorScheme.outlineVariant,
        halo = MaterialTheme.colorScheme.surfaceContainerLow,
    )
    val direction = LocalLayoutDirection.current

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val middleWidth = (maxWidth - leftWidth - rightWidth).coerceAtLeast(ManiculeSize.touchTargetMin)
        // Every visible label owns enough width at the current font scale.
        val slotWidth = maxOf(middleWidth / buckets.size, ManiculeSize.touchTargetMin, widestXLabel + ManiculeSpacing.sm)
        val metrics = ChartMetrics(
            bookStep,
            pageStep,
            leftWidth,
            rightWidth,
            middleWidth,
            slotWidth,
            chartHeight,
            plotInset,
            labelHeight,
            labelStyle,
            direction,
            onXLabelMeasured = { observedXWidth = maxOf(observedXWidth, it) },
            onBookTickMeasured = { width, height ->
                observedBookWidth = maxOf(observedBookWidth, width)
                observedTickHeight = maxOf(observedTickHeight, height)
            },
            onPageTickMeasured = { width, height ->
                observedPageWidth = maxOf(observedPageWidth, width)
                observedTickHeight = maxOf(observedTickHeight, height)
            },
        )
        // The query key must be part of the saveable registry identity. LazyColumn can dispose
        // this item while a new query arrives, so rememberSaveable(inputs = key) is insufficient.
        key(key.period, key.start, key.end, key.unit) {
            ChartViewport(buckets, key.unit, metrics, colors)
        }
    }
}

@Composable
private fun ChartViewport(
    buckets: List<ReadingChartBucket>,
    unit: ReadingChartUnit,
    metrics: ChartMetrics,
    colors: ChartColors,
) {
    val state = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState(buckets.lastIndex)
    }
    Row(verticalAlignment = Alignment.Top) {
        if (metrics.direction == LayoutDirection.Ltr) {
            AxisLabels(metrics.bookStep, metrics.leftWidth, Alignment.End, metrics, metrics.onBookTickMeasured)
        } else {
            AxisLabels(metrics.pageStep, metrics.rightWidth, Alignment.End, metrics, metrics.onPageTickMeasured)
        }
        LazyRow(
            state = state,
            modifier = Modifier.width(metrics.middleWidth).testTag("reading_chart_row"),
        ) {
            itemsIndexed(
                buckets,
                key = { _, bucket -> bucket.start.toString() },
                contentType = { _, _ -> "chart-bucket" },
            ) { index, bucket ->
                ChartBucket(bucket, buckets.getOrNull(index - 1), buckets.getOrNull(index + 1), unit, metrics, colors)
            }
        }
        if (metrics.direction == LayoutDirection.Ltr) {
            AxisLabels(metrics.pageStep, metrics.rightWidth, Alignment.Start, metrics, metrics.onPageTickMeasured)
        } else {
            AxisLabels(metrics.bookStep, metrics.leftWidth, Alignment.Start, metrics, metrics.onBookTickMeasured)
        }
    }
}

@Composable
private fun ChartBucket(
    bucket: ReadingChartBucket,
    previous: ReadingChartBucket?,
    next: ReadingChartBucket?,
    unit: ReadingChartUnit,
    metrics: ChartMetrics,
    colors: ChartColors,
) {
    val description = stringResource(
        R.string.stats_chart_bucket_description,
        bucket.start.toString(),
        bucket.end.toString(),
        bucket.bookCount,
        bucket.pagesRead,
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(metrics.slotWidth).semantics { contentDescription = description },
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(metrics.chartHeight)) {
            drawBucket(bucket, previous, next, metrics, colors)
        }
        Spacer(Modifier.height(ManiculeSpacing.xs))
        Text(
            text = chartLabel(bucket, unit),
            style = metrics.labelStyle,
            color = colors.axis,
            maxLines = 1,
            onTextLayout = { result -> metrics.onXLabelMeasured(ceil(result.multiParagraph.width).toInt()) },
        )
    }
}

private fun DrawScope.drawBucket(
    bucket: ReadingChartBucket,
    previous: ReadingChartBucket?,
    next: ReadingChartBucket?,
    metrics: ChartMetrics,
    colors: ChartColors,
) {
    val inset = metrics.plotInset.toPx()
    val plotHeight = size.height - inset * 2

    fun y(
        value: Double,
        top: Double,
    ): Float = inset + (plotHeight * (1.0 - value / top)).toFloat()
    for (tick in 0..4) {
        val yy = y(tick.toDouble(), 4.0)
        drawLine(
            colors.grid,
            Offset(0f, yy),
            Offset(size.width, yy),
            CHART_GRID_LINE_WIDTH.toPx(),
            pathEffect = PathEffect.dashPathEffect(
                floatArrayOf(ManiculeBorder.dashOn.toPx(), ManiculeBorder.dashOff.toPx()),
            ),
        )
    }
    val center = size.width / 2
    val bookY = y(bucket.bookCount.toDouble(), metrics.bookStep.toDouble() * 4.0)
    if (bucket.bookCount > 0) {
        drawRect(
            colors.bar,
            topLeft = Offset(center - CHART_BAR_WIDTH.toPx() / 2, bookY),
            size = Size(CHART_BAR_WIDTH.toPx(), size.height - inset - bookY),
        )
    }
    val pageTop = metrics.pageStep.toDouble() * 4.0
    val pointY = y(bucket.pagesRead.toDouble(), pageTop)

    fun segment(
        other: ReadingChartBucket,
        physicalX: Float,
    ) {
        val neighborY = y(other.pagesRead.toDouble(), pageTop)
        val endY = (pointY + neighborY) / 2
        drawLine(colors.halo, Offset(center, pointY), Offset(physicalX, endY), CHART_LINE_HALO_WIDTH.toPx())
        drawLine(colors.line, Offset(center, pointY), Offset(physicalX, endY), CHART_LINE_WIDTH.toPx())
    }
    previous?.let { segment(it, if (metrics.direction == LayoutDirection.Ltr) 0f else size.width) }
    next?.let { segment(it, if (metrics.direction == LayoutDirection.Ltr) size.width else 0f) }
    drawCircle(
        colors.halo,
        radius = CHART_LINE_HALO_WIDTH.toPx() / 2 + CHART_GRID_LINE_WIDTH.toPx(),
        center = Offset(center, pointY),
    )
    drawCircle(colors.line, radius = CHART_LINE_WIDTH.toPx(), center = Offset(center, pointY))
}

@Composable
private fun AxisLabels(
    step: Long,
    width: Dp,
    alignment: Alignment.Horizontal,
    metrics: ChartMetrics,
    onMeasured: (Int, Int) -> Unit,
) {
    Box(modifier = Modifier.width(width).height(metrics.chartHeight)) {
        for (tick in 0..4) {
            val label = tickLabel(step, tick)
            val y = metrics.plotInset +
                (metrics.chartHeight - metrics.plotInset * 2) * ((4 - tick) / 4f) - metrics.tickLabelHeight / 2
            Text(
                text = label,
                style = metrics.labelStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(if (alignment == Alignment.End) Alignment.TopEnd else Alignment.TopStart)
                    .offset(y = y),
                onTextLayout = { result ->
                    onMeasured(ceil(result.multiParagraph.width).toInt(), result.size.height)
                },
            )
        }
    }
}

private fun chartLabel(
    bucket: ReadingChartBucket,
    unit: ReadingChartUnit,
): String =
    when (unit) {
        ReadingChartUnit.MONTH -> "${bucket.start.year}/${bucket.start.monthNumber}"
        else -> "${bucket.start.monthNumber}/${bucket.start.dayOfMonth}"
    }

@ManiculePreview
@Composable
private fun ReadingChartPreview() {
    val start = LocalDate(2026, 9, 1)
    ManiculePreviewTheme {
        ReadingChart(
            buckets = listOf(
                ReadingChartBucket(start, start, 2, 38),
                ReadingChartBucket(LocalDate(2026, 9, 2), LocalDate(2026, 9, 2), 1, 80),
            ),
            key = ChartKey(StatsPeriod.CUSTOM, start, LocalDate(2026, 9, 2), ReadingChartUnit.DAY),
        )
    }
}
