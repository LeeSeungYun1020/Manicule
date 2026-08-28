package com.leeseungyun1020.manicule.core.notifications

import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import androidx.work.workDataOf
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import kotlinx.coroutines.flow.first
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
            enqueue(time, ExistingWorkPolicy.REPLACE)
        }

        override suspend fun scheduleNext(time: LocalTime) {
            val existingWork = workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME).first()
            if (existingWork.any { it.state == WorkInfo.State.BLOCKED }) return
            enqueue(time, ExistingWorkPolicy.APPEND_OR_REPLACE)
        }

        override suspend fun cancel() {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME).await()
        }

        private suspend fun enqueue(
            time: LocalTime,
            policy: ExistingWorkPolicy,
        ) {
            val timeZone = clock.timeZone()
            val request =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInitialDelay(
                        calculateInitialDelayMillis(clock.now(), timeZone, time),
                        TimeUnit.MILLISECONDS,
                    ).setInputData(
                        workDataOf(ReminderWorker.SCHEDULED_TIME_ZONE_ID to timeZone.id),
                    ).build()

            workManager.enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                policy,
                request,
            ).await()
        }

        companion object {
            const val UNIQUE_WORK_NAME = "daily-reading-reminder"
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
