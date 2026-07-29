package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

/**
 * 데이터가 비어있을 때 표시하는 공통 빈 상태 컴포넌트.
 *
 * @param actionLabel null 이 아니면 클릭 가능한 안내 텍스트로 [onActionClick] 을 트리거.
 *                     예: "지금 책을 추가해 보세요." → 검색창 포커스
 */
@Composable
fun ManiculeEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    ManiculeDashedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
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
            if (actionLabel != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                ManiculeButton(onClick = onActionClick, text = actionLabel)
            }
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeEmptyStatePreview() {
    ManiculeTheme {
        Box(Modifier.padding(16.dp)) {
            ManiculeEmptyState(
                title = "서재가 비어 있어요",
                description = "책을 검색하거나 스캔해 보세요",
                actionLabel = "검색",
                onActionClick = {},
                icon = {
                    Icon(
                        imageVector = ManiculeIcons.Tab.LibraryFilled,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp),
                    )
                },
            )
        }
    }
}
