package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class PermissionSnackbar(
    private val scope: CoroutineScope,
    private val hostState: SnackbarHostState,
) {
    private var job: Job? = null

    fun show(message: String) {
        if (job?.isActive == true) return
        job = scope.launch {
            hostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }
}
