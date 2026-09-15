package com.leeseungyun1020.manicule.feature.settings

import android.os.Build
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationPermissionTest {
    @Test
    fun rationaleRequired_explainsBeforeRequesting() {
        assertThat(notificationPermissionAction(Build.VERSION_CODES.TIRAMISU, false, shouldShowRationale = true))
            .isEqualTo(NotificationPermissionAction.SHOW_RATIONALE)
    }

    @Test
    fun permissionGrantedInSettings_overridesRationale() {
        assertThat(notificationPermissionAction(Build.VERSION_CODES.TIRAMISU, true, shouldShowRationale = true))
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
    fun dismissedPrompt_nextAttemptStillRequestsPermission() {
        repeat(2) {
            assertThat(notificationPermissionAction(Build.VERSION_CODES.TIRAMISU, false, shouldShowRationale = false))
                .isEqualTo(NotificationPermissionAction.REQUEST_PERMISSION)
        }
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
