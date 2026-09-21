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
 * 임의의 오류 상태를 표시할 때 사용하는 범용 에러 컴포넌트.
 *
 * [ManiculeEmptyState] 점선 카드 서식을 기반으로 일관된 에러 아이콘, 타이틀, 설명, 재시도 액션을 제공한다.
 */
@Composable
fun ManiculeErrorState(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    description: String? = null,
    retryText: String = stringResource(R.string.core_designsystem_retry),
    onRetry: (() -> Unit)? = null,
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

/**
 * 네트워크 장애 및 연결 실패 시 사용하는 공통 에러 컴포넌트.
 *
 * [ManiculeErrorState]를 기반으로 기본 네트워크 오류 아이콘과 공통 안내 문구를 제공한다.
 */
@Composable
fun ManiculeNetworkErrorState(
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
) {
    ManiculeErrorState(
        title = stringResource(R.string.core_designsystem_network_error_title),
        icon = ManiculeIcons.NetworkError,
        modifier = modifier,
        description = stringResource(R.string.core_designsystem_network_error_description),
        retryText = stringResource(R.string.core_designsystem_retry),
        onRetry = onRetry,
    )
}

@ManiculePreview
@Composable
private fun ManiculeErrorStateWithRetryPreview() {
    ManiculePreviewTheme {
        Box(Modifier.padding(MaterialTheme.spacing.lg)) {
            ManiculeErrorState(
                title = "카메라를 사용할 수 없어요",
                icon = ManiculeIcons.CameraOff,
                description = "설정에서 카메라 권한을 허용해 주세요",
                onRetry = {},
                retryText = "설정 열기",
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
                title = "지원하지 않는 기기예요",
                icon = ManiculeIcons.CameraOff,
                description = "카메라가 없는 기기에서는 바코드를 스캔할 수 없어요",
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeNetworkErrorStatePreview() {
    ManiculePreviewTheme {
        Box(Modifier.padding(MaterialTheme.spacing.lg)) {
            ManiculeNetworkErrorState(
                onRetry = {},
            )
        }
    }
}
