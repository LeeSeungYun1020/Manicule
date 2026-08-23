package com.leeseungyun1020.manicule.core.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 26)
class NotificationChannelTest {
    private lateinit var context: Context
    private lateinit var manager: NotificationManager
    private lateinit var publisher: AndroidReminderNotificationPublisher

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        manager = context.getSystemService(NotificationManager::class.java)
        manager.deleteNotificationChannel(ReminderNotificationChannel.ID)
        publisher = AndroidReminderNotificationPublisher(context, ReminderNotificationChannel(context))
    }

    @Test
    fun publishingWithoutScheduling_createsChannel() {
        publisher.publish(emptyList())

        assertThat(manager.getNotificationChannel(ReminderNotificationChannel.ID)).isNotNull()
    }
}
