package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.feature.bookdetail.R

@Composable
internal fun BookDetailRatingBar(
    rating: Int,
    isSaving: Boolean,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateDescriptionText =
        when {
            isSaving -> stringResource(R.string.book_detail_rating_saving)
            rating > 0 -> stringResource(R.string.book_detail_rating_current_state, rating)
            else -> stringResource(R.string.book_detail_rating_empty_state)
        }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (star in 1..5) {
            val isCurrentRating = star == rating
            val actionLabel =
                if (isCurrentRating) {
                    stringResource(R.string.book_detail_rating_action_clear)
                } else {
                    stringResource(R.string.book_detail_rating_action_select, star)
                }
            val starContentDescription = stringResource(R.string.book_detail_rating_star_description, star)

            IconButton(
                onClick = { onRatingSelected(star) },
                enabled = !isSaving,
                modifier =
                    Modifier
                        .minimumInteractiveComponentSize()
                        .semantics {
                            contentDescription = starContentDescription
                            stateDescription = stateDescriptionText
                            onClick(label = actionLabel) {
                                if (!isSaving) {
                                    onRatingSelected(star)
                                    true
                                } else {
                                    false
                                }
                            }
                        },
            ) {
                Icon(
                    imageVector = if (star <= rating) ManiculeIcons.Star else ManiculeIcons.StarBorder,
                    contentDescription = null,
                    tint =
                        if (star <= rating) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                )
            }
        }
    }
}

@ManiculePreview
@Composable
private fun BookDetailRatingBarZeroPreview() {
    ManiculePreviewTheme {
        BookDetailRatingBar(
            rating = 0,
            isSaving = false,
            onRatingSelected = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailRatingBarRatedPreview() {
    ManiculePreviewTheme {
        BookDetailRatingBar(
            rating = 4,
            isSaving = false,
            onRatingSelected = {},
        )
    }
}

@ManiculePreview
@Composable
private fun BookDetailRatingBarSavingPreview() {
    ManiculePreviewTheme {
        BookDetailRatingBar(
            rating = 3,
            isSaving = true,
            onRatingSelected = {},
        )
    }
}
