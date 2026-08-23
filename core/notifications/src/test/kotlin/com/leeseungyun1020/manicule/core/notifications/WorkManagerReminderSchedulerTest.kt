package com.leeseungyun1020.manicule.core.notifications

import android.app.NotificationManager
import android.content.Context
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WorkManagerReminderSchedulerTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: WorkManagerReminderScheduler

    @Before
    fun setUp() {
        context = RuntimeEnvironment.getApplication()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, Configuration.Builder().build())
        workManager = WorkManager.getInstance(context)
        scheduler =
            WorkManagerReminderScheduler(
                workManager = workManager,
                clock = FakeClock(Instant.parse("2026-08-09T10:00:00Z"), TimeZone.UTC),
                notificationChannel = ReminderNotificationChannel(context),
            )
    }

    @Test
    fun schedule_createsReminderNotificationChannel() =
        runTest {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.deleteNotificationChannel(ReminderNotificationChannel.ID)

            scheduler.schedule(LocalTime(11, 0))

            val channel = manager.getNotificationChannel(ReminderNotificationChannel.ID)
            assertThat(channel.name).isEqualTo(context.getString(R.string.reminder_channel_name))
            assertThat(channel.description).isEqualTo(context.getString(R.string.reminder_channel_description))
            assertThat(channel.importance).isEqualTo(NotificationManager.IMPORTANCE_DEFAULT)
        }

    @Test
    fun schedule_registersSingleUniquePeriodicWorkAndUpdatesIt() =
        runTest {
            scheduler.schedule(LocalTime(11, 0))
            scheduler.schedule(LocalTime(12, 0))

            val work = workManager.getWorkInfosForUniqueWork(WorkManagerReminderScheduler.UNIQUE_WORK_NAME).get()

            assertThat(work).hasSize(1)
            assertThat(work.single().state).isEqualTo(WorkInfo.State.ENQUEUED)
        }

    @Test
    fun cancel_cancelsUniqueWork() =
        runTest {
            scheduler.schedule(LocalTime(11, 0))

            scheduler.cancel()

            val work = workManager.getWorkInfosForUniqueWork(WorkManagerReminderScheduler.UNIQUE_WORK_NAME).get()
            assertThat(work.single().state).isEqualTo(WorkInfo.State.CANCELLED)
        }

    @Test
    fun initialDelay_beforeSelectedTime_targetsToday() {
        val delay =
            calculateInitialDelayMillis(
                now = Instant.parse("2026-08-09T10:00:00Z"),
                timeZone = TimeZone.UTC,
                scheduledTime = LocalTime(10, 30),
            )

        assertThat(delay).isEqualTo(TimeUnit.MINUTES.toMillis(30))
    }

    @Test
    fun initialDelay_atSelectedTime_targetsTomorrow() {
        val delay =
            calculateInitialDelayMillis(
                now = Instant.parse("2026-08-09T10:00:00Z"),
                timeZone = TimeZone.UTC,
                scheduledTime = LocalTime(10, 0),
            )

        assertThat(delay).isEqualTo(TimeUnit.HOURS.toMillis(24))
    }

    @Test
    fun initialDelay_afterSelectedTime_crossesMidnight() {
        val delay =
            calculateInitialDelayMillis(
                now = Instant.parse("2026-08-09T23:30:00Z"),
                timeZone = TimeZone.UTC,
                scheduledTime = LocalTime(1, 0),
            )

        assertThat(delay).isEqualTo(TimeUnit.MINUTES.toMillis(90))
    }

    @Test
    fun initialDelay_usesCurrentTimeZone() {
        val delay =
            calculateInitialDelayMillis(
                now = Instant.parse("2026-08-09T23:30:00Z"),
                timeZone = TimeZone.of("Asia/Seoul"),
                scheduledTime = LocalTime(9, 0),
            )

        assertThat(delay).isEqualTo(TimeUnit.MINUTES.toMillis(30))
    }
}

private class FakeClock(
    private val instant: Instant,
    private val zone: TimeZone,
) : Clock {
    override fun now(): Instant = instant

    override fun timeZone(): TimeZone = zone
}
