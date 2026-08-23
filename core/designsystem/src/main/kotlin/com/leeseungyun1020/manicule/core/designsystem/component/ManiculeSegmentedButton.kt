package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSpacing
import com.leeseungyun1020.manicule.core.designsystem.theme.border
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

/**
 * Manicule 전용 Segmented Button.
 *
 * @param options 선택 가능한 항목 리스트
 * @param selectedOption 현재 선택된 항목
 * @param onOptionSelected 항목이 선택되었을 때 호출되는 콜백
 * @param modifier Modifier
 * @param itemLabel 항목을 텍스트로 변환하는 함수 (기본값: toString())
 */
@Composable
fun <T> ManiculeSegmentedButton(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    itemLabel: (T) -> String = { it.toString() },
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .border(
                    width = MaterialTheme.border.ring,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = MaterialTheme.shapes.medium,
                ).clip(MaterialTheme.shapes.medium)
                .selectableGroup(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        options.forEach { option ->
            val isSelected = option == selectedOption
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = ManiculeSize.touchTargetMin)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        ).selectable(
                            selected = isSelected,
                            onClick = { onOptionSelected(option) },
                            role = Role.RadioButton,
                        ).padding(vertical = MaterialTheme.spacing.sm),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = itemLabel(option),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeSegmentedButtonPreview() {
    ManiculePreviewTheme {
        Column(
            modifier = Modifier.padding(ManiculeSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(ManiculeSpacing.lg),
        ) {
            var selectedOption1 by remember { mutableStateOf("읽는 중") }
            ManiculeSegmentedButton(
                options = listOf("읽고 싶음", "읽는 중", "다 읽음"),
                selectedOption = selectedOption1,
                onOptionSelected = { selectedOption1 = it },
            )

            var selectedOption2 by remember { mutableStateOf("읽고 싶음") }
            ManiculeSegmentedButton(
                options = listOf("읽고 싶음", "읽는 중", "다 읽음"),
                selectedOption = selectedOption2,
                onOptionSelected = { selectedOption2 = it },
            )
        }
    }
}
