package com.leeseungyun1020.manicule.feature.settings

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

internal enum class NotificationPermissionAction {
    ENABLE_REMINDER,
    REQUEST_PERMISSION,
    SHOW_RATIONALE,
}

internal fun notificationPermissionAction(
    context: Context,
    activity: Activity?,
): NotificationPermissionAction {
    val permissionGranted =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    val shouldShowRationale =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true
    return notificationPermissionAction(Build.VERSION.SDK_INT, permissionGranted, shouldShowRationale)
}

internal fun notificationPermissionAction(
    sdkInt: Int,
    permissionGranted: Boolean,
    shouldShowRationale: Boolean = false,
): NotificationPermissionAction =
    if (sdkInt < Build.VERSION_CODES.TIRAMISU || permissionGranted) {
        NotificationPermissionAction.ENABLE_REMINDER
    } else if (shouldShowRationale) {
        NotificationPermissionAction.SHOW_RATIONALE
    } else {
        NotificationPermissionAction.REQUEST_PERMISSION
    }
