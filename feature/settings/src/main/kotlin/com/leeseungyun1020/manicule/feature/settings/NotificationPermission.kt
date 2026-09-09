package com.leeseungyun1020.manicule.feature.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings

internal enum class NotificationPermissionAction {
    ENABLE_REMINDER,
    REQUEST_PERMISSION,
    SHOW_SETTINGS,
}

internal fun notificationPermissionAction(
    context: Context,
    activity: Activity?,
    previouslyDenied: Boolean,
): NotificationPermissionAction {
    val permissionGranted =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val shouldShowRationale =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true
    return notificationPermissionAction(Build.VERSION.SDK_INT, permissionGranted, previouslyDenied || shouldShowRationale)
}

internal fun notificationPermissionAction(
    sdkInt: Int,
    permissionGranted: Boolean,
    previouslyDenied: Boolean = false,
): NotificationPermissionAction =
    if (sdkInt < Build.VERSION_CODES.TIRAMISU || permissionGranted) {
        NotificationPermissionAction.ENABLE_REMINDER
    } else if (previouslyDenied) {
        NotificationPermissionAction.SHOW_SETTINGS
    } else {
        NotificationPermissionAction.REQUEST_PERMISSION
    }

internal fun appNotificationSettingsIntent(packageName: String): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
