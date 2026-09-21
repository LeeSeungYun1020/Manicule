package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize

private val BOOKMARK_RIBBON_WIDTH = 9.dp
private val BOOKMARK_RIBBON_MIN_HEIGHT = 6.dp
private val BOOKMARK_RIBBON_NOTCH_DEPTH = 3.dp
private val DEFAULT_BOOKMARK_RIBBON_SHAPE = BookmarkRibbonShape()

/**
 * 리본 끝이 역오각형(아래쪽으로 뾰족한 화살표 형태)으로 마감되는 책갈피 Shape.
 */
internal data class BookmarkRibbonShape(
    private val notchDepth: Dp = BOOKMARK_RIBBON_NOTCH_DEPTH,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val notchPx = with(density) { notchDepth.toPx() }
        val points = bookmarkRibbonPoints(size = size, notchDepth = notchPx)
        val path =
            Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { point -> lineTo(point.x, point.y) }
                close()
            }
        return Outline.Generic(path)
    }
}

internal fun bookmarkRibbonPoints(
    size: Size,
    notchDepth: Float,
): List<Offset> {
    val notchStartY = (size.height - notchDepth).coerceAtLeast(0f)
    return listOf(
        Offset.Zero,
        Offset(size.width, 0f),
        Offset(size.width, notchStartY),
        Offset(size.width / 2f, size.height),
        Offset(0f, notchStartY),
    )
}

@Composable
fun BookmarkRibbon(
    progress: Float,
    modifier: Modifier = Modifier,
    coverHeight: Dp = BookCoverSize.Medium.height,
) {
    if (progress <= 0f) return

    val clampedProgress = progress.coerceIn(0f, 1f)
    val calculatedHeight = (coverHeight * clampedProgress).coerceAtLeast(BOOKMARK_RIBBON_MIN_HEIGHT)

    Box(
        modifier =
            modifier
                .width(BOOKMARK_RIBBON_WIDTH)
                .height(calculatedHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = DEFAULT_BOOKMARK_RIBBON_SHAPE,
                ).testTag("bookmark_ribbon"),
    )
}

@ManiculePreview
@Composable
private fun BookmarkRibbonPreview() {
    ManiculePreviewTheme {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            listOf(0.08f, 0.64f, 1f).forEach { progress ->
                Box(
                    modifier =
                        Modifier
                            .width(BookCoverSize.Medium.width)
                            .height(BookCoverSize.Medium.height)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    BookmarkRibbon(progress = progress)
                }
            }
        }
    }
}
