package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.feature.library.R

@Composable
internal fun LibrarySort.Criterion.label(): String =
    when (this) {
        LibrarySort.Criterion.ADDED_AT -> stringResource(R.string.library_sort_added_at)
        LibrarySort.Criterion.UPDATED_AT -> stringResource(R.string.library_sort_updated_at)
        LibrarySort.Criterion.RATING -> stringResource(R.string.library_sort_rating)
    }

@Composable
internal fun LibrarySort.Direction.label(criterion: LibrarySort.Criterion): String =
    when (this) {
        LibrarySort.Direction.DESCENDING ->
            when (criterion) {
                LibrarySort.Criterion.ADDED_AT,
                LibrarySort.Criterion.UPDATED_AT,
                -> stringResource(R.string.library_sort_latest)
                LibrarySort.Criterion.RATING -> stringResource(R.string.library_sort_highest)
            }
        LibrarySort.Direction.ASCENDING ->
            when (criterion) {
                LibrarySort.Criterion.ADDED_AT,
                LibrarySort.Criterion.UPDATED_AT,
                -> stringResource(R.string.library_sort_oldest)
                LibrarySort.Criterion.RATING -> stringResource(R.string.library_sort_lowest)
            }
    }
