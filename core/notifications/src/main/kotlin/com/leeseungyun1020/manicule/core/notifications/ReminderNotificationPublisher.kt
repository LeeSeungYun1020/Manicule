package com.leeseungyun1020.manicule.core.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ReminderNotificationPublisher {
    fun publish(content: ReminderContent)
}

class AndroidReminderNotificationPublisher
    @Inject
    constructor(
        @param:ApplicationContext private val context: Context,
    ) : ReminderNotificationPublisher {
        override fun publish(content: ReminderContent) {
            createChannel()
            if (!canPostNotification()) return

            val notification =
                NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(context.applicationInfo.icon)
                    .setContentTitle(context.getString(R.string.reminder_notification_title))
                    .setContentText(content.message())
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .apply {
                        launchPendingIntent()?.let(::setContentIntent)
                    }.build()

            runCatching { postNotification(notification) }
        }

        @SuppressLint("MissingPermission")
        private fun postNotification(notification: Notification) {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }

        private fun ReminderContent.message(): String =
            when (this) {
                is ReminderContent.Book -> context.getString(R.string.reminder_notification_book_message, title)
                ReminderContent.Generic -> context.getString(R.string.reminder_notification_generic_message)
            }

        private fun createChannel() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.reminder_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT,
                ).apply {
                    description = context.getString(R.string.reminder_channel_description)
                }
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }

        private fun canPostNotification(): Boolean {
            val hasPermission =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
            val manager = NotificationManagerCompat.from(context)
            val channelEnabled =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context
                        .getSystemService(NotificationManager::class.java)
                        .getNotificationChannel(CHANNEL_ID)
                        ?.importance != NotificationManager.IMPORTANCE_NONE
                } else {
                    true
                }
            return shouldPostNotification(hasPermission, manager.areNotificationsEnabled(), channelEnabled)
        }

        private fun launchPendingIntent(): PendingIntent? =
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.let { launchIntent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    launchIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )
            }

        companion object {
            const val CHANNEL_ID = "reading_reminders"
            const val NOTIFICATION_ID = 1001
        }
    }

internal fun shouldPostNotification(
    hasPermission: Boolean,
    appNotificationsEnabled: Boolean,
    channelEnabled: Boolean,
): Boolean = hasPermission && appNotificationsEnabled && channelEnabled
