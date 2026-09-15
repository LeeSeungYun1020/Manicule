package com.leeseungyun1020.manicule.feature.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings

internal enum class NotificationPermissionAction {
    ENABLE_REMINDER,
    REQUEST_PERMISSION,
    SHOW_RATIONALE,
}

internal fun notificationPermissionAction(
    context: Context,
    activity: Activity?,
): NotificationPermissionAction {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        return NotificationPermissionAction.ENABLE_REMINDER
    }
    val permissionGranted =
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val shouldShowRationale =
        activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true
    return notificationPermissionAction(Build.VERSION.SDK_INT, permissionGranted, shouldShowRationale)
}

internal fun notificationPermissionAction(
    sdkInt: Int,
    permissionGranted: Boolean,
    shouldShowRationale: Boolean = false,
): NotificationPermissionAction =
    when {
        sdkInt < Build.VERSION_CODES.TIRAMISU || permissionGranted -> NotificationPermissionAction.ENABLE_REMINDER
        shouldShowRationale -> NotificationPermissionAction.SHOW_RATIONALE
        else -> NotificationPermissionAction.REQUEST_PERMISSION
    }

internal fun appNotificationSettingsIntent(packageName: String): Intent =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
    }
