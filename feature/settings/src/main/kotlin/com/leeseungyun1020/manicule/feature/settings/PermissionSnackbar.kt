package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class PermissionSnackbar(
    private val scope: CoroutineScope,
    private val hostState: SnackbarHostState,
) {
    private var job: Job? = null

    fun dismiss() {
        job?.cancel()
        job = null
    }

    fun show(
        message: String,
        actionLabel: String? = null,
        onAction: () -> Unit = {},
    ) {
        if (job?.isActive == true) return
        job = scope.launch {
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = actionLabel != null,
                duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            job = null
            if (result == SnackbarResult.ActionPerformed) onAction()
        }
    }
}
