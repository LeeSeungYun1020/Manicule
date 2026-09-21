package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.ui.book.BookCoverSize

private const val OVERLAY_HEIGHT_FRACTION = 0.35f
private const val OVERLAY_DIM_ALPHA = 0.78f
private val OVERLAY_CONTENT_COLOR = Color.White
private val OVERLAY_END_PADDING = 6.dp
private val OVERLAY_BOTTOM_PADDING = 5.dp

@Composable
fun BookCoverStatusOverlay(
    text: String,
    modifier: Modifier = Modifier,
) {
    val scrim = MaterialTheme.colorScheme.scrim
    val overlayBrush =
        remember(scrim) {
            Brush.verticalGradient(
                listOf(
                    scrim.copy(alpha = 0f),
                    scrim.copy(alpha = OVERLAY_DIM_ALPHA),
                ),
            )
        }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .fillMaxHeight(OVERLAY_HEIGHT_FRACTION)
                .background(overlayBrush)
                .padding(end = OVERLAY_END_PADDING, bottom = OVERLAY_BOTTOM_PADDING)
                .testTag("book_cover_status_overlay"),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Text(
            text = text,
            color = OVERLAY_CONTENT_COLOR,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.testTag("book_cover_status_overlay_text"),
        )
    }
}

@ManiculePreview
@Composable
private fun BookCoverStatusOverlayPreview() {
    ManiculePreviewTheme {
        Box(
            modifier =
                Modifier
                    .padding(MaterialTheme.spacing.lg)
                    .size(BookCoverSize.Medium.width, BookCoverSize.Medium.height)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            BookCoverStatusOverlay(
                text = "64%",
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}
