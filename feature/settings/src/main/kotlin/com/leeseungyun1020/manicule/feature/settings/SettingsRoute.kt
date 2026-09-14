package com.leeseungyun1020.manicule.feature.settings

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ManagedActivityResultLauncher
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
import com.leeseungyun1020.manicule.feature.settings.components.NotificationPermissionRationale
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SettingsRoute(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsRouteContent(uiState = uiState, viewModel = viewModel)
}

@Composable
private fun SettingsRouteContent(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var showRationale by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionSnackbar = remember(scope, snackbarHostState) { PermissionSnackbar(scope, snackbarHostState) }
    val permissionRequiredMessage = stringResource(R.string.settings_notification_permission_denied)
    val settingsLabel = stringResource(R.string.settings_open_system_settings)

    val permissionLauncher =
        rememberSettingsPermissionLauncher(
            context = context,
            activity = activity,
            onGranted = { viewModel.setReminderEnabled(true) },
            onDenied = {
                permissionSnackbar.show(permissionRequiredMessage, actionLabel = settingsLabel) {
                    openNotificationSettings(context, permissionSnackbar, permissionRequiredMessage)
                }
            },
        )

    if (showRationale) {
        NotificationPermissionRationale(
            onContinue = {
                showRationale = false
                if (notificationPermissionAction(context, activity) == NotificationPermissionAction.ENABLE_REMINDER) {
                    viewModel.setReminderEnabled(true)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onDismiss = { showRationale = false },
        )
    }

    SettingsScreenContainer(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        viewModel = viewModel,
        onToggle = { enabled ->
            permissionSnackbar.dismiss()
            if (!enabled) {
                viewModel.setReminderEnabled(false)
            } else {
                when (notificationPermissionAction(context, activity)) {
                    NotificationPermissionAction.ENABLE_REMINDER -> {
                        viewModel.setReminderEnabled(true)
                    }
                    NotificationPermissionAction.SHOW_RATIONALE ->
                        showRationale = true
                    NotificationPermissionAction.REQUEST_PERMISSION ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                }
            }
        },
    )
}

@Composable
private fun SettingsScreenContainer(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    viewModel: SettingsViewModel,
    onToggle: (Boolean) -> Unit,
) {
    SettingsSnackbarEffect(viewModel = viewModel, snackbarHostState = snackbarHostState)
    SettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onReminderEnabledChange = onToggle,
        onReminderTimeChange = viewModel::setReminderTime,
        onRetryPreferences = viewModel::retryPreferences,
    )
}

@Composable
private fun rememberSettingsPermissionLauncher(
    context: Context,
    activity: Activity?,
    onGranted: () -> Unit,
    onDenied: () -> Unit,
): ManagedActivityResultLauncher<String, Boolean> =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        val isGranted = notificationPermissionAction(context, activity) == NotificationPermissionAction.ENABLE_REMINDER
        if (granted && isGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }

private fun openNotificationSettings(
    context: Context,
    permissionSnackbar: PermissionSnackbar,
    fallbackMessage: String,
) {
    try {
        context.startActivity(appNotificationSettingsIntent(context.packageName))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(
                Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", context.packageName, null),
                ),
            )
        } catch (_: Exception) {
            permissionSnackbar.show(fallbackMessage)
        }
    }
}

@Composable
private fun SettingsSnackbarEffect(
    viewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState,
) {
    val updateFailedMessage = stringResource(R.string.settings_reminder_update_failed)
    val retryLabel = stringResource(R.string.settings_retry)
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
