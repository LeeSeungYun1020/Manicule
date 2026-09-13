package com.leeseungyun1020.manicule.feature.settings

import android.Manifest
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current
    var permissionDenied by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionSnackbar = remember(scope, snackbarHostState) { PermissionSnackbar(scope, snackbarHostState) }
    val permissionDeniedMessage = stringResource(R.string.settings_notification_permission_denied)
    val openSettingsLabel = stringResource(R.string.settings_open_system_settings)
    val updateFailedMessage = stringResource(R.string.settings_reminder_update_failed)
    val retryLabel = stringResource(R.string.settings_retry)

    fun showPermissionDeniedMessage() {
        permissionSnackbar.show(permissionDeniedMessage, openSettingsLabel) {
            context.startActivity(appNotificationSettingsIntent(context.packageName))
        }
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            permissionDenied = !granted
            if (granted) {
                viewModel.setReminderEnabled(true)
            } else {
                showPermissionDeniedMessage()
            }
        }

    SettingsSnackbarEffect(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        updateFailedMessage = updateFailedMessage,
        retryLabel = retryLabel,
    )

    SettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onReminderEnabledChange = { enabled ->
            if (!enabled) {
                viewModel.setReminderEnabled(false)
            } else {
                when (notificationPermissionAction(context, activity, permissionDenied)) {
                    NotificationPermissionAction.ENABLE_REMINDER -> viewModel.setReminderEnabled(true)
                    NotificationPermissionAction.SHOW_SETTINGS -> showPermissionDeniedMessage()
                    NotificationPermissionAction.REQUEST_PERMISSION ->
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        },
        onReminderTimeChange = viewModel::setReminderTime,
        onRetryPreferences = viewModel::retryPreferences,
    )
}

@Composable
private fun SettingsSnackbarEffect(
    viewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState,
    updateFailedMessage: String,
    retryLabel: String,
) {
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
                    when (result) {
                        SnackbarResult.ActionPerformed -> viewModel.retryReminderUpdate(event)
                        SnackbarResult.Dismissed -> viewModel.dismissReminderUpdateFailure(event)
                    }
                }
                SettingsEvent.DismissReminderUpdateFailure -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                }
            }
        }
    }
}
