package com.leeseungyun1020.manicule.core.notifications

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerReminderScheduler
    @Inject
    internal constructor(
        private val workManager: WorkManager,
        private val clock: Clock,
        private val notificationChannel: ReminderNotificationChannel,
    ) : ReminderScheduler {
        override suspend fun schedule(time: LocalTime) {
            notificationChannel.ensureCreated()

            val request =
                PeriodicWorkRequestBuilder<ReminderWorker>(REPEAT_INTERVAL_HOURS, TimeUnit.HOURS)
                    .setInitialDelay(
                        calculateInitialDelayMillis(clock.now(), clock.timeZone(), time),
                        TimeUnit.MILLISECONDS,
                    ).build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        override suspend fun cancel() {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
        }

        companion object {
            const val UNIQUE_WORK_NAME = "daily-reading-reminder"
            private const val REPEAT_INTERVAL_HOURS = 24L
        }
    }

internal fun calculateInitialDelayMillis(
    now: Instant,
    timeZone: TimeZone,
    scheduledTime: LocalTime,
): Long {
    val currentDate = now.toLocalDateTime(timeZone).date
    val todayTarget = LocalDateTime(currentDate, scheduledTime).toInstant(timeZone)
    val nextTarget =
        if (todayTarget > now) {
            todayTarget
        } else {
            LocalDateTime(currentDate.plus(1, DateTimeUnit.DAY), scheduledTime).toInstant(timeZone)
        }
    return nextTarget.toEpochMilliseconds() - now.toEpochMilliseconds()
}
