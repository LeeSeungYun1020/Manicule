package com.leeseungyun1020.manicule.feature.library.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTabRow
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.library.R

private val libraryStatuses = listOf(ReadingStatus.WANT, ReadingStatus.READING, ReadingStatus.FINISHED)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("LongParameterList")
internal fun LibraryTopBar(
    selectedStatus: ReadingStatus,
    sort: LibrarySort,
    hasBooks: Boolean,
    onStatusSelected: (ReadingStatus) -> Unit,
    onSortClick: () -> Unit,
    onSearch: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val tabLabels =
        mapOf(
            ReadingStatus.WANT to stringResource(R.string.library_tab_want),
            ReadingStatus.READING to stringResource(R.string.library_tab_reading),
            ReadingStatus.FINISHED to stringResource(R.string.library_tab_finished),
            ReadingStatus.UNSET to "",
        )
    Column {
        ManiculeTopAppBar(
            title = stringResource(R.string.library_title),
            scrollBehavior = scrollBehavior,
            actions = {
                if (hasBooks) {
                    ManiculeIconButton(onClick = onSortClick) {
                        Icon(
                            imageVector = ManiculeIcons.Sort,
                            contentDescription = stringResource(R.string.library_sort_title),
                        )
                    }
                }
            },
        )
        ManiculeTabRow(
            tabs = libraryStatuses,
            selectedTabIndex = libraryStatuses.indexOf(selectedStatus),
            onTabSelected = { onStatusSelected(libraryStatuses[it]) },
            tabLabel = { tabLabels.getValue(it) },
        )
        if (hasBooks) {
            LibraryActionRow(sort, onSearch)
        }
    }
}

@Composable
private fun LibraryActionRow(
    sort: LibrarySort,
    onSearch: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.lg),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text =
                stringResource(
                    R.string.library_sort_caption,
                    sort.criterion.label(),
                    sort.direction.label(sort.criterion),
                ),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ManiculeIconButton(onClick = onSearch) {
            Icon(
                imageVector = ManiculeIcons.Add,
                contentDescription = stringResource(R.string.library_add_book),
            )
        }
    }
}
