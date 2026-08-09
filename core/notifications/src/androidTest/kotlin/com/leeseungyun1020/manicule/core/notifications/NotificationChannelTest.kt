package com.leeseungyun1020.manicule.core.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationChannelTest {
    @Test
    fun publishingTwice_keepsSingleChannel() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val publisher = AndroidReminderNotificationPublisher(context)

        publisher.publish(ReminderContent.Generic)
        publisher.publish(ReminderContent.Generic)

        val manager = context.getSystemService(NotificationManager::class.java)
        val channels = manager.notificationChannels.filter { it.id == AndroidReminderNotificationPublisher.CHANNEL_ID }
        assertThat(channels).hasSize(1)
    }
}
