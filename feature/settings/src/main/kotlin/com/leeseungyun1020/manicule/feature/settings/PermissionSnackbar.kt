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

    fun show(
        message: String,
        actionLabel: String,
        onOpenSettings: () -> Unit,
    ) {
        if (job?.isActive == true) return
        job = scope.launch {
            val result = hostState.showSnackbar(message, actionLabel, duration = SnackbarDuration.Indefinite)
            if (result == SnackbarResult.ActionPerformed) onOpenSettings()
        }
    }
}
