package com.leeseungyun1020.manicule.core.ui.book

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.maniculeColors
import com.leeseungyun1020.manicule.core.ui.R

enum class BookCoverSize(
    val width: Dp,
    val height: Dp,
) {
    Small(64.dp, 92.dp),
    Medium(100.dp, 150.dp),
}

@Composable
fun BookCover(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    showBorder: Boolean = false,
    placeholder: Painter? = null,
) {
    val actualPlaceholder = placeholder ?: ColorPainter(MaterialTheme.maniculeColors.coverPlaceholder)
    val finalModifier = if (showBorder) {
        modifier.border(1.dp, MaterialTheme.maniculeColors.coverBorder)
    } else {
        modifier
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
        Box(modifier = Modifier.padding(16.dp)) {
            BookCover(
                imageUrl = null,
                modifier = Modifier.size(100.dp, 150.dp),
            )
        }
    }
}

@ManiculePreview
@Composable
private fun BookCoverImagePreview() {
    ManiculeTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BookCover(
                imageUrl = "https://nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/2025/02/9791161759692.jpg",
                modifier = Modifier.size(100.dp, 150.dp),
                placeholder = painterResource(id = R.drawable.sample_book_cover),
            )
        }
    }
}
