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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.rememberTextMeasurer
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

@Composable
fun ReadingChart(
    buckets: List<ReadingChartBucket>,
    key: ChartKey,
    modifier: Modifier = Modifier,
) {
    if (buckets.isEmpty()) return
    val bookStep = remember(buckets) { chartTickStep(buckets.maxOf { it.bookCount.toLong() }) }
    val pageStep = remember(buckets) { chartTickStep(buckets.maxOf { it.pagesRead }) }
    val bookTop = bookStep.toDouble() * 4.0
    val pageTop = pageStep.toDouble() * 4.0
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val labelStyle = MaterialTheme.typography.labelSmall
    val leftWidth = with(density) {
        (0..4).maxOf { textMeasurer.measure(tickLabel(bookStep, it), labelStyle).size.width }.toDp() + ManiculeSpacing.sm
    }
    val rightWidth = with(density) {
        (0..4).maxOf { textMeasurer.measure(tickLabel(pageStep, it), labelStyle).size.width }.toDp() + ManiculeSpacing.sm
    }
    val axisColor = MaterialTheme.colorScheme.onSurfaceVariant
    val barColor = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.onSurface
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val haloColor = MaterialTheme.colorScheme.surfaceContainerLow
    val direction = LocalLayoutDirection.current
    val state = rememberSaveable(key, saver = LazyListState.Saver) {
        LazyListState(buckets.lastIndex)
    }
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val middleWidth = maxWidth - leftWidth - rightWidth
        val slotWidth = maxOf(middleWidth / buckets.size, ManiculeSize.touchTargetMin)
        val widestLabel = buckets.maxOf { bucket ->
            val label = chartLabel(bucket, key.unit)
            textMeasurer.measure(label, labelStyle).size.width
        }
        val stride = max(1, ceil(widestLabel.toDouble() / with(density) { slotWidth.toPx() }).toInt())
        // Keep the two numeric axes at physical left and right even when the data row is RTL.
        Row(verticalAlignment = Alignment.Top) {
            if (direction == LayoutDirection.Ltr) {
                AxisLabels(bookStep, leftWidth, Alignment.End)
            } else {
                AxisLabels(pageStep, rightWidth, Alignment.End)
            }
            LazyRow(
                state = state,
                modifier = Modifier.width(middleWidth),
            ) {
                itemsIndexed(buckets, key = {
                    _,
                    bucket,
                    ->
                    bucket.start.toString()
                }, contentType = { _, _ -> "chart-bucket" }) { index, bucket ->
                    val description = stringResource(
                        R.string.stats_chart_bucket_description,
                        bucket.start.toString(),
                        bucket.end.toString(),
                        bucket.bookCount,
                        bucket.pagesRead,
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(slotWidth).semantics { contentDescription = description },
                    ) {
                        Canvas(modifier = Modifier.fillMaxWidth().height(ManiculeSize.chartHeight)) {
                            val inset = ManiculeSpacing.sm.toPx()
                            val plotHeight = size.height - inset * 2

                            fun y(
                                value: Double,
                                top: Double,
                            ): Float = inset + (plotHeight * (1.0 - value / top)).toFloat()
                            for (tick in 0..4) {
                                val yy = y(tick.toDouble(), 4.0)
                                drawLine(
                                    gridColor,
                                    Offset(0f, yy),
                                    Offset(size.width, yy),
                                    ManiculeBorder.hairline.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(ManiculeBorder.dashOn.toPx(), ManiculeBorder.dashOff.toPx()),
                                    ),
                                )
                            }
                            val center = size.width / 2
                            val bookY = y(bucket.bookCount.toDouble(), bookTop)
                            if (bucket.bookCount > 0) {
                                drawRect(
                                    barColor,
                                    topLeft = Offset(center - ManiculeSize.chartBarWidth.toPx() / 2, bookY),
                                    size = androidx.compose.ui.geometry.Size(
                                        ManiculeSize.chartBarWidth.toPx(),
                                        size.height - inset - bookY,
                                    ),
                                )
                            }
                            val pointY = y(bucket.pagesRead.toDouble(), pageTop)
                            val previous = buckets.getOrNull(index - 1)
                            val next = buckets.getOrNull(index + 1)

                            fun segment(
                                other: ReadingChartBucket,
                                physicalX: Float,
                            ) {
                                val neighborY = y(other.pagesRead.toDouble(), pageTop)
                                val endY = (pointY + neighborY) / 2
                                drawLine(haloColor, Offset(center, pointY), Offset(physicalX, endY), ManiculeSize.chartLineHaloWidth.toPx())
                                drawLine(lineColor, Offset(center, pointY), Offset(physicalX, endY), ManiculeSize.chartLineWidth.toPx())
                            }
                            previous?.let { segment(it, if (direction == LayoutDirection.Ltr) 0f else size.width) }
                            next?.let { segment(it, if (direction == LayoutDirection.Ltr) size.width else 0f) }
                            drawCircle(
                                haloColor,
                                radius = ManiculeSize.chartLineHaloWidth.toPx() / 2 + ManiculeBorder.hairline.toPx(),
                                center = Offset(center, pointY),
                            )
                            drawCircle(lineColor, radius = ManiculeSize.chartLineWidth.toPx(), center = Offset(center, pointY))
                        }
                        Spacer(Modifier.height(ManiculeSpacing.xs))
                        Text(
                            text = if (index % stride == 0) chartLabel(bucket, key.unit) else "",
                            style = labelStyle,
                            color = axisColor,
                            maxLines = 1,
                        )
                    }
                }
            }
            if (direction == LayoutDirection.Ltr) {
                AxisLabels(pageStep, rightWidth, Alignment.Start)
            } else {
                AxisLabels(bookStep, leftWidth, Alignment.Start)
            }
        }
    }
}

@Composable
private fun AxisLabels(
    step: Long,
    width: androidx.compose.ui.unit.Dp,
    alignment: Alignment.Horizontal,
) {
    Box(modifier = Modifier.width(width).height(ManiculeSize.chartHeight)) {
        for (tick in 0..4) {
            Text(
                text = tickLabel(step, tick),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(if (alignment == Alignment.End) Alignment.TopEnd else Alignment.TopStart)
                    .offset(y = (ManiculeSize.chartHeight - ManiculeSpacing.sm * 2) * ((4 - tick) / 4f)),
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
