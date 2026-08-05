package com.leeseungyun1020.manicule.core.ui.book

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeBorder
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.ui.R

enum class BookCoverSize(
    val width: Dp,
    val height: Dp,
) {
    Small(ManiculeSize.coverSmallWidth, ManiculeSize.coverSmallHeight),
    Medium(ManiculeSize.coverMediumWidth, ManiculeSize.coverMediumHeight),
}

@Composable
fun BookCover(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    showBorder: Boolean = false,
    placeholder: Painter? = null,
    size: BookCoverSize = BookCoverSize.Medium,
) {
    val actualPlaceholder = placeholder ?: ColorPainter(MaterialTheme.colorScheme.surfaceVariant)
    val sizedModifier = modifier.size(size.width, size.height)
    val finalModifier =
        if (showBorder) {
            sizedModifier.border(ManiculeBorder.cover, MaterialTheme.colorScheme.outlineVariant)
        } else {
            sizedModifier
        }

    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = finalModifier,
        placeholder = actualPlaceholder,
        error = actualPlaceholder,
        fallback = actualPlaceholder,
        contentScale = ContentScale.Crop,
    )
}

@ManiculePreview
@Composable
private fun BookCoverPlaceholderPreview() {
    ManiculeTheme {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            BookCover(
                imageUrl = null,
                size = BookCoverSize.Small,
            )
            BookCover(
                imageUrl = null,
                size = BookCoverSize.Medium,
            )
        }
    }
}

@ManiculePreview
@Composable
private fun BookCoverImagePreview() {
    ManiculeTheme {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        ) {
            BookCover(
                imageUrl = "https://nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/2025/02/9791161759692.jpg",
                size = BookCoverSize.Small,
                placeholder = painterResource(id = R.drawable.sample_book_cover),
            )
            BookCover(
                imageUrl = "https://nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/2025/02/9791161759692.jpg",
                size = BookCoverSize.Medium,
                placeholder = painterResource(id = R.drawable.sample_book_cover),
            )
        }
    }
}
