package com.leeseungyun1020.manicule.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.leeseungyun1020.manicule.core.common.di.ApplicationScope
import com.leeseungyun1020.manicule.core.domain.settings.GetActiveReminderTimeUseCase
import com.leeseungyun1020.manicule.core.domain.settings.ReminderScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import javax.inject.Inject

@AndroidEntryPoint
class TimeZoneChangedReceiver : BroadcastReceiver() {
    @Inject
    internal lateinit var runner: TimeZoneChangedReceiverRunner

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if (intent.action != Intent.ACTION_TIMEZONE_CHANGED) return

        val pendingResult = goAsync()
        applicationScope.launch {
            runTimeChangeBroadcast(
                run = runner::run,
                finish = pendingResult::finish,
            )
        }
    }
}

internal suspend fun runTimeChangeBroadcast(
    run: suspend () -> Unit,
    finish: () -> Unit,
) {
    runCatching { run() }
        .also { finish() }
        .onFailure { if (it is CancellationException) throw it }
}

internal class TimeZoneChangedReceiverRunner
    @Inject
    constructor(
        private val getActiveReminderTime: GetActiveReminderTimeUseCase,
        private val reminderScheduler: ReminderScheduler,
    ) {
        suspend fun run() {
            rescheduleReminderAfterTimeZoneChange(
                getActiveTime = getActiveReminderTime::invoke,
                schedule = reminderScheduler::schedule,
            )
        }
    }

internal suspend fun rescheduleReminderAfterTimeZoneChange(
    getActiveTime: suspend () -> LocalTime?,
    schedule: suspend (LocalTime) -> Unit,
) {
    getActiveTime()?.let { schedule(it) }
}
