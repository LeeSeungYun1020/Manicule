package com.leeseungyun1020.manicule.feature.settings

import android.content.Intent
import android.os.Build
import android.provider.Settings

internal enum class NotificationPermissionAction {
    ENABLE_REMINDER,
    REQUEST_PERMISSION,
}

internal fun notificationPermissionAction(
    sdkInt: Int,
    permissionGranted: Boolean,
): NotificationPermissionAction =
    if (sdkInt < Build.VERSION_CODES.TIRAMISU || permissionGranted) {
        NotificationPermissionAction.ENABLE_REMINDER
    } else {
        NotificationPermissionAction.REQUEST_PERMISSION
    }

internal fun appNotificationSettingsIntent(packageName: String): Intent =
    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
