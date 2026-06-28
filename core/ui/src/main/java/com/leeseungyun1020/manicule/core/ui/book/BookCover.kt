package com.leeseungyun1020.manicule.core.ui.book

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.leeseungyun1020.manicule.core.ui.R

@Composable
fun BookCover(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    placeholder: Painter = ColorPainter(MaterialTheme.colorScheme.primaryContainer),
) {
    AsyncImage(
        model = imageUrl,
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Crop,
    )
}

@Preview(name = "With Placeholder", showBackground = true)
@Composable
private fun BookCoverPlaceholderPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BookCover(
                imageUrl = null,
                modifier = Modifier.size(100.dp, 150.dp),
            )
        }
    }
}

@Preview(name = "With Image", showBackground = true)
@Composable
private fun BookCoverImagePreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            BookCover(
                imageUrl = "https://nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/2025/02/9791161759692.jpg",
                modifier = Modifier.size(100.dp, 150.dp),
                placeholder = painterResource(id = R.drawable.sample_book_cover),
            )
        }
    }
}
