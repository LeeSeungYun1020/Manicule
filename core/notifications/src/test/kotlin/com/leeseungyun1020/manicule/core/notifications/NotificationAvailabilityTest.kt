package com.leeseungyun1020.manicule.core.notifications

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class NotificationAvailabilityTest {
    @Test
    fun notificationCanPost_whenEverySystemGateIsEnabled() {
        assertThat(
            shouldPostNotification(
                hasPermission = true,
                appNotificationsEnabled = true,
                channelEnabled = true,
            ),
        ).isTrue()
    }

    @Test
    fun notificationIsSkipped_withoutRuntimePermission() {
        assertThat(
            shouldPostNotification(
                hasPermission = false,
                appNotificationsEnabled = true,
                channelEnabled = true,
            ),
        ).isFalse()
    }

    @Test
    fun notificationIsSkipped_whenAppNotificationsAreBlocked() {
        assertThat(
            shouldPostNotification(
                hasPermission = true,
                appNotificationsEnabled = false,
                channelEnabled = true,
            ),
        ).isFalse()
    }

    @Test
    fun notificationIsSkipped_whenChannelIsBlocked() {
        assertThat(
            shouldPostNotification(
                hasPermission = true,
                appNotificationsEnabled = true,
                channelEnabled = false,
            ),
        ).isFalse()
    }
}
