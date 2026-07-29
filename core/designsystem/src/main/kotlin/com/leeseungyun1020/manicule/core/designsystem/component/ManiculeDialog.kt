package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculeTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

/**
 * Manicule 의 모든 다이얼로그가 통일된 형태를 갖도록 하는 공통 다이얼로그.
 *
 * - 책 삭제 ("기록한 내용이 모두 삭제되어요"),
 * - 완독 확인 ("혹시 책을 다 읽으셨나요?"),
 * - 스캔 실패 ("ISBN 인식에 실패하였어요") 등에서 동일하게 사용.
 *
 * @param dismissText null 이면 dismiss 버튼을 숨기고 confirm 만 노출 (정보용 다이얼로그).
 */
@Composable
fun ManiculeDialog(
    onDismissRequest: () -> Unit,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = modifier,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(MaterialTheme.spacing.xl),
            ) {
                if (icon != null) {
                    icon()
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                }
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.xl))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    if (dismissText != null) {
                        ManiculeOutlinedButton(
                            onClick = { (onDismiss ?: onDismissRequest).invoke() },
                            text = dismissText,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    ManiculeButton(
                        onClick = onConfirm,
                        text = confirmText,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeDialogPreview() {
    ManiculeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ManiculeDialog(
                onDismissRequest = {},
                title = "책 삭제",
                message = "기록한 내용이 모두 삭제되어요",
                confirmText = "삭제",
                onConfirm = {},
                dismissText = "취소",
                onDismiss = {},
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeIconDialogPreview() {
    ManiculeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ManiculeDialog(
                onDismissRequest = {},
                icon = {
                    Icon(
                        imageVector = ManiculeIcons.Star,
                        contentDescription = null,
                        modifier = Modifier.padding(bottom = 8.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
                title = "혹시 책을 다 읽었나요?",
                message = "다 읽었다면 '다 읽음'으로 표시할게요",
                confirmText = "네",
                onConfirm = {},
                dismissText = "아니요",
                onDismiss = {},
            )
        }
    }
}

@ManiculePreview
@Composable
private fun ManiculeSingleButtonDialogPreview() {
    ManiculeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            ManiculeDialog(
                onDismissRequest = {},
                title = "오류",
                message = "바코드를 인식하지 못했어요",
                confirmText = "확인",
                onConfirm = {},
            )
        }
    }
}
