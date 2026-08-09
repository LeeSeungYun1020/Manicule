package com.leeseungyun1020.manicule.core.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.leeseungyun1020.manicule.core.domain.settings.GetReminderContentUseCase
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReminderWorker
    @AssistedInject
    constructor(
        @Assisted appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val getReminderContent: GetReminderContentUseCase,
        private val notificationPublisher: ReminderNotificationPublisher,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result =
            if (
                runReminder(
                    getContent = { getReminderContent() },
                    publish = notificationPublisher::publish,
                )
            ) {
                Result.success()
            } else {
                Result.retry()
            }
    }

internal suspend fun runReminder(
    getContent: suspend () -> ReminderContent,
    publish: (ReminderContent) -> Unit,
): Boolean {
    val content = runCatching { getContent() }.getOrElse { return false }
    runCatching { publish(content) }
    return true
}
