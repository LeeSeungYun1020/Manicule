package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize

/**
 * 리본 끝이 역오각형(아래쪽으로 뾰족한 화살표 형태)으로 마감되는 책갈피 Shape.
 */
class BookmarkRibbonShape(
    private val notchDepth: Dp = 3.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val notchPx = with(density) { notchDepth.toPx() }
        val path =
            Path().apply {
                moveTo(0f, 0f)
                lineTo(size.width, 0f)
                lineTo(size.width, (size.height - notchPx).coerceAtLeast(0f))
                lineTo(size.width / 2f, size.height)
                lineTo(0f, (size.height - notchPx).coerceAtLeast(0f))
                close()
            }
        return Outline.Generic(path)
    }
}

val BookmarkRibbonWidth = 9.dp
val BookmarkRibbonMinHeight = 6.dp

@Composable
fun BookmarkRibbon(
    progress: Float,
    modifier: Modifier = Modifier,
    coverHeight: Dp = BookCoverSize.Medium.height,
) {
    if (progress <= 0f) return

    val clampedProgress = progress.coerceIn(0f, 1f)
    val calculatedHeight = (coverHeight * clampedProgress).coerceAtLeast(BookmarkRibbonMinHeight)

    Box(
        modifier =
            modifier
                .width(BookmarkRibbonWidth)
                .height(calculatedHeight)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = BookmarkRibbonShape(),
                ).testTag("bookmark_ribbon"),
    )
}
