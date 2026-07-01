package com.leeseungyun1020.manicule.core.ui.book

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun BookProgressBar(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    val progress =
        if (totalPages > 0) {
            (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$currentPage / ${totalPages}쪽",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BookProgressBarPreview() {
    MaterialTheme {
        BookProgressBar(
            currentPage = 132,
            totalPages = 320,
            modifier = Modifier.padding(16.dp),
        )
    }
}
