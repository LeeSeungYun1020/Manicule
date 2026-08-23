package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

/**
 * 데이터가 비어있을 때 표시하는 공통 빈 상태 컴포넌트.
 *
 * @param actions 빈 상태에서 제공할 선택적 동작. 한두 개의 버튼을 배치할 때 사용한다.
 */
@Composable
fun ManiculeEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: (@Composable () -> Unit)? = null,
    actions: (@Composable FlowRowScope.() -> Unit)? = null,
) {
    ManiculeDashedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (icon != null) {
                icon()
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
            if (description != null) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.sm))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (actions != null) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                    content = actions,
                )
            }
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeEmptyStateWithoutActionPreview() {
    ManiculePreviewTheme {
        Box(Modifier.padding(MaterialTheme.spacing.lg)) {
            ManiculeEmptyState(
                title = "기록이 없어요",
                description = "책을 읽고 첫 기록을 남겨 보세요",
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeEmptyStateWithActionPreview() {
    ManiculePreviewTheme {
        Box(Modifier.padding(MaterialTheme.spacing.lg)) {
            ManiculeEmptyState(
                title = "서재가 비어 있어요",
                description = "책을 검색하거나 스캔해 보세요",
                icon = {
                    Icon(
                        imageVector = ManiculeIcons.Tab.LibraryFilled,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(ManiculeSize.iconEmptyState),
                    )
                },
                actions = {
                    ManiculeButton(onClick = {}, text = "검색")
                },
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeEmptyStateWithTwoActionsPreview() {
    ManiculePreviewTheme {
        Box(Modifier.padding(MaterialTheme.spacing.lg)) {
            ManiculeEmptyState(
                title = "카메라 권한이 필요해요",
                description = "권한을 허용하거나 책을 직접 검색해 보세요",
                actions = {
                    ManiculeButton(onClick = {}, text = "카메라 사용")
                    ManiculeOutlinedButton(onClick = {}, text = "검색")
                },
            )
        }
    }
}
