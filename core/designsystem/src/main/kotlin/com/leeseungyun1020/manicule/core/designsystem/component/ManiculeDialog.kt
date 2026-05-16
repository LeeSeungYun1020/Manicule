package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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
    dismissText: String? = null,
    onDismiss: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(text = confirmText) }
        },
        dismissButton =
            if (dismissText != null) {
                {
                    TextButton(onClick = { (onDismiss ?: onDismissRequest).invoke() }) {
                        Text(text = dismissText)
                    }
                }
            } else {
                null
            },
    )
}
