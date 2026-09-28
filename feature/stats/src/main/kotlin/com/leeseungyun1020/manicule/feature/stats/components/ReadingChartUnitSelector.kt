package com.leeseungyun1020.manicule.feature.stats.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartUnit
import com.leeseungyun1020.manicule.feature.stats.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ReadingChartUnitSelector(
    selectedUnit: ReadingChartUnit,
    onUnitSelected: (ReadingChartUnit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(
        R.string.stats_chart_day_short to R.string.stats_chart_day_description,
        R.string.stats_chart_week_short to R.string.stats_chart_week_description,
        R.string.stats_chart_month_short to R.string.stats_chart_month_description,
    )
    SingleChoiceSegmentedButtonRow(modifier = modifier.testTag("reading_chart_unit_selector")) {
        ReadingChartUnit.entries.forEachIndexed { index, unit ->
            val (label, description) = labels[index]
            val accessibilityLabel = stringResource(description)
            SegmentedButton(
                selected = selectedUnit == unit,
                onClick = { onUnitSelected(unit) },
                shape = SegmentedButtonDefaults.itemShape(index, ReadingChartUnit.entries.size),
                modifier = Modifier
                    .widthIn(min = MaterialTheme.size.touchTargetMin)
                    .heightIn(min = MaterialTheme.size.touchTargetMin)
                    .semantics { contentDescription = accessibilityLabel }
                    .testTag("reading_chart_unit_${unit.name.lowercase()}"),
                icon = {},
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.sm),
                label = { Text(stringResource(label)) },
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ReadingChartUnitSelectorPreview() {
    ManiculePreviewTheme {
        ReadingChartUnitSelector(selectedUnit = ReadingChartUnit.WEEK, onUnitSelected = {})
    }
}
