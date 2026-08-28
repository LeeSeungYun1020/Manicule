package com.leeseungyun1020.manicule.core.designsystem.component

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme

@Composable
fun ManiculeSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
    )
}

suspend fun SnackbarHostState.showUndoSnackbar(
    message: String,
    undoLabel: String,
    duration: SnackbarDuration = SnackbarDuration.Long,
): SnackbarResult {
    currentSnackbarData?.dismiss()
    return showSnackbar(
        message = message,
        actionLabel = undoLabel,
        duration = duration,
    )
}

@ManiculePreview
@Composable
private fun ManiculeSnackbarHostPreview() {
    ManiculePreviewTheme {
        val hostState = remember { SnackbarHostState() }
        LaunchedEffect(hostState) {
            hostState.showUndoSnackbar(
                message = "책을 삭제했어요",
                undoLabel = "실행 취소",
            )
        }
        ManiculeSnackbarHost(hostState = hostState)
    }
}
