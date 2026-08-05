package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

/**
 * 통계 값과 라벨을 하나의 접근성 항목으로 표시한다.
 *
 * @param value 강조할 통계 값
 * @param label 값의 의미를 설명하는 라벨
 * @param icon 값 위에 표시할 선택적 아이콘 슬롯
 */
@Composable
fun ManiculeStatTile(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
) {
    ManiculeCard(
        modifier = modifier.semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.sm, vertical = MaterialTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.xs))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeStatTilePreview() {
    ManiculeTheme {
        ManiculeStatTile(
            value = "12일",
            label = "연속 기록",
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
            icon = {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    modifier = Modifier.size(ManiculeSize.iconSm),
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )
    }
}

@ManiculePreview
@Composable
private fun ManiculeStatTileWithoutIconPreview() {
    ManiculeTheme {
        ManiculeStatTile(
            value = "1,248p",
            label = "읽은 페이지",
            modifier = Modifier.padding(MaterialTheme.spacing.lg),
        )
    }
}
