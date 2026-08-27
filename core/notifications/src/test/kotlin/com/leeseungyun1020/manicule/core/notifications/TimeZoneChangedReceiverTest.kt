package com.leeseungyun1020.manicule.core.notifications

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Test

class TimeZoneChangedReceiverTest {
    @Test
    fun enabledReminder_reschedulesAtActiveTime() =
        runTest {
            val activeTime = LocalTime(21, 0)
            var scheduledTime: LocalTime? = null

            rescheduleReminderAfterTimeZoneChange(
                getActiveTime = { activeTime },
                schedule = { scheduledTime = it },
            )

            assertThat(scheduledTime).isEqualTo(activeTime)
        }

    @Test
    fun disabledReminder_doesNotSchedule() =
        runTest {
            var scheduled = false

            rescheduleReminderAfterTimeZoneChange(
                getActiveTime = { null },
                schedule = { scheduled = true },
            )

            assertThat(scheduled).isFalse()
        }

    @Test
    fun reschedulingFailure_isConsumedAndFinishesBroadcast() =
        runTest {
            var finished = false

            runTimeChangeBroadcast(
                run = { error("rescheduling failed") },
                finish = { finished = true },
            )

            assertThat(finished).isTrue()
        }
}
