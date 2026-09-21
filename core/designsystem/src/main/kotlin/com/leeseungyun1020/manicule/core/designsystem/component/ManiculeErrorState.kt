package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.leeseungyun1020.manicule.core.designsystem.R
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeSize
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

/**
 * 네트워크 장애, 초기 로드 실패 등 화면 전체를 표시할 수 없을 때 사용하는 공통 에러 컴포넌트.
 *
 * [ManiculeEmptyState] 점선 카드 서식을 기반으로 일관된 에러 아이콘, 타이틀, 설명, 재시도 액션을 제공한다.
 */
@Composable
fun ManiculeErrorState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = ManiculeIcons.NetworkError,
    onRetry: (() -> Unit)? = null,
    retryText: String = stringResource(R.string.core_designsystem_retry),
) {
    ManiculeEmptyState(
        title = title,
        description = description,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(ManiculeSize.iconEmptyState),
            )
        },
        actions =
            if (onRetry != null) {
                {
                    ManiculeButton(
                        onClick = onRetry,
                        text = retryText,
                        leadingIcon = {
                            Icon(
                                imageVector = ManiculeIcons.Refresh,
                                contentDescription = null,
                            )
                        },
                    )
                }
            } else {
                null
            },
    )
}

@ManiculePreview
@Composable
private fun ManiculeErrorStateWithRetryPreview() {
    ManiculePreviewTheme {
        Box(Modifier.padding(MaterialTheme.spacing.lg)) {
            ManiculeErrorState(
                title = "검색 결과를 불러올 수 없어요",
                description = "잠시 후 다시 시도해 주세요",
                onRetry = {},
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeErrorStateWithoutRetryPreview() {
    ManiculePreviewTheme {
        Box(Modifier.padding(MaterialTheme.spacing.lg)) {
            ManiculeErrorState(
                title = "네트워크 연결 오류",
                description = "인터넷 연결 상태를 확인해 주세요",
            )
        }
    }
}
