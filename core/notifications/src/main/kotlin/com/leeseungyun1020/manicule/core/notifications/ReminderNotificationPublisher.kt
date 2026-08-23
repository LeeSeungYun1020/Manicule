package com.leeseungyun1020.manicule.core.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

interface ReminderNotificationPublisher {
    fun publish(books: List<ReminderContent.Book>)
}

class AndroidReminderNotificationPublisher
    @Inject
    internal constructor(
        @param:ApplicationContext private val context: Context,
        private val notificationChannel: ReminderNotificationChannel,
    ) : ReminderNotificationPublisher {
        override fun publish(books: List<ReminderContent.Book>) {
            notificationChannel.ensureCreated()
            if (!canPostNotification()) return

            val message = books.message()
            val notification =
                NotificationCompat.Builder(context, ReminderNotificationChannel.ID)
                    .setSmallIcon(R.drawable.ic_notification_reminder)
                    .setContentTitle(context.getString(R.string.reminder_notification_title))
                    .setContentText(message)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .apply {
                        launchPendingIntent()?.let(::setContentIntent)
                    }.build()

            runCatching { postNotification(notification) }
        }

        private fun postNotification(notification: Notification) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }

        private fun List<ReminderContent.Book>.message(): String =
            when (size) {
                0 -> context.getString(R.string.reminder_notification_generic_message)
                1 -> context.getString(R.string.reminder_notification_book_message, first().title)
                2 -> let { context.getString(R.string.reminder_notification_books_message, it[0].title, it[1].title) }
                else -> shuffled().take(2).let { context.getString(R.string.reminder_notification_books_message, it[0].title, it[1].title) }
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
                        .getNotificationChannel(ReminderNotificationChannel.ID)
                        ?.let { it.importance != NotificationManager.IMPORTANCE_NONE }
                        ?: false
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
            const val NOTIFICATION_ID = 1001
        }
    }

internal fun shouldPostNotification(
    hasPermission: Boolean,
    appNotificationsEnabled: Boolean,
    channelEnabled: Boolean,
): Boolean = hasPermission && appNotificationsEnabled && channelEnabled
