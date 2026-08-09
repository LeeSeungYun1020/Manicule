package com.leeseungyun1020.manicule.core.ui.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.ui.R
import kotlin.math.roundToInt

@Composable
fun BookProgressBar(
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier,
) {
    if (totalPages <= 0) return

    val safeCurrentPage = currentPage.coerceAtLeast(0)
    val progress = (safeCurrentPage.toDouble() / totalPages).coerceIn(0.0, 1.0)
    val percentage = (progress * 100).roundToInt()

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { progress.toFloat() },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(ManiculeSize.progressBarThick),
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(id = R.string.book_progress_text, safeCurrentPage, totalPages),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(id = R.string.book_progress_percentage, percentage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ManiculePreview
@Composable
private fun BookProgressBarPreview() {
    ManiculeTheme {
        Column(
            modifier = Modifier.padding(ManiculeSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ManiculeSpacing.lg),
        ) {
            BookProgressBar(currentPage = 132, totalPages = 320)
            BookProgressBar(currentPage = 400, totalPages = 320)
        }
    }
}
