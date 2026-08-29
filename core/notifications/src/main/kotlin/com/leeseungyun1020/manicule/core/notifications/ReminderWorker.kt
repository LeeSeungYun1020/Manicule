package com.leeseungyun1020.manicule.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.domain.settings.GetActiveReminderTimeUseCase
import com.leeseungyun1020.manicule.core.domain.settings.GetReminderContentUseCase
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@HiltWorker
class ReminderWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val runner: ReminderWorkerRunner,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result =
            if (runner.run(inputData.getString(SCHEDULED_TIME_ZONE_ID))) {
                Result.success()
            } else {
                Result.retry()
            }

        companion object {
            internal const val SCHEDULED_TIME_ZONE_ID = "scheduled-time-zone-id"
        }
    }

class ReminderWorkerRunner
    @Inject
    internal constructor(
        private val getActiveReminderTime: GetActiveReminderTimeUseCase,
        private val getReminderContent: GetReminderContentUseCase,
        private val reminderScheduler: ReminderScheduler,
        private val notificationPublisher: ReminderNotificationPublisher,
        private val clock: Clock,
    ) {
        suspend fun run(scheduledTimeZoneId: String?): Boolean =
            runReminder(
                timeZones =
                    ReminderTimeZones(
                        scheduled = scheduledTimeZoneId,
                        current = clock.timeZone().id,
                    ),
                getActiveTime = { getActiveReminderTime() },
                getContent = { getReminderContent() },
                actions =
                    ReminderScheduleActions(
                        schedule = reminderScheduler::schedule,
                        scheduleNext = reminderScheduler::scheduleNext,
                    ),
                publish = notificationPublisher::publish,
            )
    }

internal data class ReminderTimeZones(
    val scheduled: String?,
    val current: String,
)

internal class ReminderScheduleActions(
    val schedule: suspend (LocalTime) -> Unit,
    val scheduleNext: suspend (LocalTime) -> Unit,
)

internal suspend fun runReminder(
    timeZones: ReminderTimeZones,
    getActiveTime: suspend () -> LocalTime?,
    getContent: suspend () -> List<ReminderContent.Book>,
    actions: ReminderScheduleActions,
    publish: (List<ReminderContent.Book>) -> Unit,
): Boolean {
    val activeTime =
        runCatching { getActiveTime() }
            .onFailure { if (it is CancellationException) throw it }
            .getOrElse { return false } ?: return true

    if (timeZones.scheduled != timeZones.current) {
        return runCatching { actions.schedule(activeTime) }
            .onFailure { if (it is CancellationException) throw it }
            .isSuccess
    }

    val content =
        runCatching { getContent() }
            .onFailure { if (it is CancellationException) throw it }
            .getOrElse { return false }
    if (
        runCatching { actions.scheduleNext(activeTime) }
            .onFailure { if (it is CancellationException) throw it }
            .isFailure
    ) {
        return false
    }

    runCatching { publish(content) }
    return true
}
