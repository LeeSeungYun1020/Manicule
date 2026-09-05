package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeBottomSheet
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeOutlinedButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSegmentedButton
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.feature.library.R

private val sortCriteria = LibrarySort.Criterion.entries
private val sortDirections = listOf(LibrarySort.Direction.DESCENDING, LibrarySort.Direction.ASCENDING)

@Composable
fun SortBottomSheet(
    sort: LibrarySort,
    onSortChange: (LibrarySort) -> Unit,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManiculeBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        SortSheetContent(
            sort = sort,
            onSortChange = onSortChange,
            onDismissRequest = onDismissRequest,
            onApply = onApply,
        )
    }
}

@Composable
private fun SortSheetContent(
    sort: LibrarySort,
    onSortChange: (LibrarySort) -> Unit,
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SortSheetHeader(onDismissRequest)
        SortCriterionList(sort, onSortChange)
        SortSheetActions(onDismissRequest, onApply)
    }
}

@Composable
private fun SortSheetHeader(onDismissRequest: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.library_sort_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        ManiculeIconButton(onClick = onDismissRequest) {
            Icon(
                imageVector = ManiculeIcons.Close,
                contentDescription = stringResource(R.string.library_sort_close),
            )
        }
    }
}

@Composable
private fun SortCriterionList(
    sort: LibrarySort,
    onSortChange: (LibrarySort) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().selectableGroup()) {
        sortCriteria.forEachIndexed { index, criterion ->
            SortCriterionRow(sort, criterion, onSortChange)
            if (index < sortCriteria.lastIndex) {
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun SortCriterionRow(
    sort: LibrarySort,
    criterion: LibrarySort.Criterion,
    onSortChange: (LibrarySort) -> Unit,
) {
    val selected = criterion == sort.criterion
    val descendingLabel = LibrarySort.Direction.DESCENDING.label(criterion)
    val ascendingLabel = LibrarySort.Direction.ASCENDING.label(criterion)
    ListItem(
        headlineContent = {
            Text(
                text = criterion.label(),
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            )
        },
        modifier =
            Modifier
                .fillMaxWidth()
                .selectable(
                    selected = selected,
                    onClick = { onSortChange(sort.copy(criterion = criterion)) },
                    role = Role.RadioButton,
                ),
        trailingContent = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
    )
    AnimatedVisibility(visible = selected) {
        ManiculeSegmentedButton(
            options = sortDirections,
            selectedOption = sort.direction,
            onOptionSelected = { onSortChange(sort.copy(direction = it)) },
            modifier =
                Modifier.padding(
                    start = MaterialTheme.spacing.lg,
                    end = MaterialTheme.spacing.lg,
                    bottom = MaterialTheme.spacing.md,
                ),
            itemLabel = {
                when (it) {
                    LibrarySort.Direction.DESCENDING -> descendingLabel
                    LibrarySort.Direction.ASCENDING -> ascendingLabel
                }
            },
        )
    }
}

@Composable
private fun SortSheetActions(
    onDismissRequest: () -> Unit,
    onApply: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        ManiculeOutlinedButton(
            onClick = onDismissRequest,
            text = stringResource(R.string.library_sort_cancel),
            modifier = Modifier.weight(1f),
        )
        ManiculeButton(
            onClick = onApply,
            text = stringResource(R.string.library_sort_apply),
            modifier = Modifier.weight(1f),
        )
    }
}

@ManiculePreview
@Composable
private fun AddedAtSortSheetPreview() {
    SortSheetPreview(LibrarySort.Criterion.ADDED_AT)
}

@ManiculePreview
@Composable
private fun UpdatedAtSortSheetPreview() {
    SortSheetPreview(LibrarySort.Criterion.UPDATED_AT)
}

@ManiculePreview
@Composable
private fun RatingSortSheetPreview() {
    SortSheetPreview(LibrarySort.Criterion.RATING)
}

@Composable
private fun SortSheetPreview(criterion: LibrarySort.Criterion) {
    ManiculePreviewTheme {
        Surface {
            SortSheetContent(
                sort = LibrarySort(criterion, LibrarySort.Direction.DESCENDING),
                onSortChange = {},
                onDismissRequest = {},
                onApply = {},
            )
        }
    }
}
