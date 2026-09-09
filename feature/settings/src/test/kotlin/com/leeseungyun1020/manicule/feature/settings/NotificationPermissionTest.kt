package com.leeseungyun1020.manicule.feature.settings

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationPermissionTest {
    @Test
    fun deniedPermission_subsequentAttemptShowsSettings() {
        assertThat(notificationPermissionAction(Build.VERSION_CODES.TIRAMISU, false, previouslyDenied = true))
            .isEqualTo(NotificationPermissionAction.SHOW_SETTINGS)
    }

    @Test
    fun permissionGrantedInSettings_overridesPreviousDenial() {
        assertThat(notificationPermissionAction(Build.VERSION_CODES.TIRAMISU, true, previouslyDenied = true))
            .isEqualTo(NotificationPermissionAction.ENABLE_REMINDER)
    }

    @Test
    fun android13WithoutPermission_requestsPermission() {
        val action =
            notificationPermissionAction(
                sdkInt = Build.VERSION_CODES.TIRAMISU,
                permissionGranted = false,
            )

        assertThat(action).isEqualTo(NotificationPermissionAction.REQUEST_PERMISSION)
    }

    @Test
    fun android13WithPermission_enablesReminder() {
        val action =
            notificationPermissionAction(
                sdkInt = Build.VERSION_CODES.TIRAMISU,
                permissionGranted = true,
            )

        assertThat(action).isEqualTo(NotificationPermissionAction.ENABLE_REMINDER)
    }

    @Test
    fun android12WithoutRuntimePermission_enablesReminder() {
        val action =
            notificationPermissionAction(
                sdkInt = Build.VERSION_CODES.S_V2,
                permissionGranted = false,
            )

        assertThat(action).isEqualTo(NotificationPermissionAction.ENABLE_REMINDER)
    }
}
