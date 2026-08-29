package com.leeseungyun1020.manicule.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class ReminderNotificationChannel
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) {
        fun ensureCreated() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

            val channel =
                NotificationChannel(
                    ID,
                    context.getString(R.string.reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.reminder_channel_description)
                }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        companion object {
            const val ID = "reading_reminders"
        }
    }
