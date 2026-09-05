package com.leeseungyun1020.manicule.feature.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionDeniedMessage = stringResource(R.string.settings_notification_permission_denied)
    val openSettingsLabel = stringResource(R.string.settings_open_system_settings)
    val updateFailedMessage = stringResource(R.string.settings_reminder_update_failed)
    val retryLabel = stringResource(R.string.settings_retry)

    fun showPermissionDeniedMessage() {
        scope.launch {
            val result =
                snackbarHostState.showSnackbar(
                    message = permissionDeniedMessage,
                    actionLabel = openSettingsLabel,
                    duration = SnackbarDuration.Indefinite,
                )
            if (result == SnackbarResult.ActionPerformed) {
                context.startActivity(appNotificationSettingsIntent(context.packageName))
            }
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                viewModel.setReminderEnabled(true)
            } else {
                showPermissionDeniedMessage()
            }
        }

    LaunchedEffect(viewModel, snackbarHostState) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is SettingsEvent.ReminderUpdateFailed -> {
                    val result =
                        snackbarHostState.showSnackbar(
                            message = updateFailedMessage,
                            actionLabel = retryLabel,
                            duration = SnackbarDuration.Indefinite,
                        )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.retryReminderUpdate(event.desiredConfig)
                    }
                }
            }
        }
    }

    SettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onReminderEnabledChange = { enabled ->
            if (!enabled) {
                viewModel.setReminderEnabled(false)
            } else {
                val permissionGranted =
                    Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                when (notificationPermissionAction(Build.VERSION.SDK_INT, permissionGranted)) {
                    NotificationPermissionAction.ENABLE_REMINDER -> viewModel.setReminderEnabled(true)
                    NotificationPermissionAction.REQUEST_PERMISSION ->
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        },
        onReminderTimeChange = viewModel::setReminderTime,
        onRetryPreferences = viewModel::retryPreferences,
    )
}
