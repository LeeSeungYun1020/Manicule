package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ManiculeTabRow(
    tabs: List<T>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    tabLabel: (T) -> String = { it.toString() },
) {
    if (tabs.isEmpty() || selectedTabIndex !in tabs.indices) return

    SecondaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier,
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = index == selectedTabIndex,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = tabLabel(tab),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeTabRowPreview() {
    ManiculePreviewTheme {
        Column(
            modifier = Modifier.padding(vertical = ManiculeSpacing.lg),
        ) {
            ManiculeTabRow(
                tabs = listOf("Book information", "My records"),
                selectedTabIndex = 0,
                onTabSelected = {},
            )
            ManiculeTabRow(
                tabs = listOf("Want to read", "Reading", "Finished"),
                selectedTabIndex = 1,
                onTabSelected = {},
            )
        }
    }
}
