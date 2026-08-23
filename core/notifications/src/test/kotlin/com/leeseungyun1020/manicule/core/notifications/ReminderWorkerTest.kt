package com.leeseungyun1020.manicule.core.notifications

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalTime
import org.junit.Test

class ReminderWorkerTest {
    @Test
    fun successfulContent_isPublishedAndCompletes() =
        runTest {
            var published: ReminderContent? = null

            val completed =
                runReminder(
                    timeZones = SAME_TIME_ZONES,
                    getActiveTime = { ACTIVE_TIME },
                    getContent = { ReminderContent.Book("Book") },
                    scheduleNext = {},
                    publish = { published = it },
                )

            assertThat(completed).isTrue()
            assertThat(published).isEqualTo(ReminderContent.Book("Book"))
        }

    @Test
    fun temporaryContentError_requestsRetry() =
        runTest {
            val completed =
                runReminder(
                    timeZones = SAME_TIME_ZONES,
                    getActiveTime = { ACTIVE_TIME },
                    getContent = { error("temporary") },
                    scheduleNext = {},
                    publish = {},
                )

            assertThat(completed).isFalse()
        }

    @Test
    fun notificationPostingError_doesNotRetryContentWork() =
        runTest {
            val completed =
                runReminder(
                    timeZones = SAME_TIME_ZONES,
                    getActiveTime = { ACTIVE_TIME },
                    getContent = { ReminderContent.Generic },
                    scheduleNext = {},
                    publish = { error("notifications unavailable") },
                )

            assertThat(completed).isTrue()
        }

    @Test
    fun changedTimeZone_reschedulesWithoutPublishing() =
        runTest {
            var scheduledTime: LocalTime? = null
            var published = false

            val completed =
                runReminder(
                    timeZones = ReminderTimeZones(TIME_ZONE_ID, "America/New_York"),
                    getActiveTime = { ACTIVE_TIME },
                    getContent = { error("content must not be loaded") },
                    scheduleNext = { scheduledTime = it },
                    publish = { published = true },
                )

            assertThat(completed).isTrue()
            assertThat(scheduledTime).isEqualTo(ACTIVE_TIME)
            assertThat(published).isFalse()
        }

    @Test
    fun disabledReminder_doesNotRescheduleOrPublish() =
        runTest {
            var scheduled = false
            var published = false

            val completed =
                runReminder(
                    timeZones = SAME_TIME_ZONES,
                    getActiveTime = { null },
                    getContent = { error("content must not be loaded") },
                    scheduleNext = { scheduled = true },
                    publish = { published = true },
                )

            assertThat(completed).isTrue()
            assertThat(scheduled).isFalse()
            assertThat(published).isFalse()
        }

    @Test
    fun nextSchedulingError_requestsRetryWithoutPublishing() =
        runTest {
            var published = false

            val completed =
                runReminder(
                    timeZones = SAME_TIME_ZONES,
                    getActiveTime = { ACTIVE_TIME },
                    getContent = { ReminderContent.Generic },
                    scheduleNext = { error("temporary") },
                    publish = { published = true },
                )

            assertThat(completed).isFalse()
            assertThat(published).isFalse()
        }

    private companion object {
        const val TIME_ZONE_ID = "Asia/Seoul"
        val SAME_TIME_ZONES = ReminderTimeZones(TIME_ZONE_ID, TIME_ZONE_ID)
        val ACTIVE_TIME = LocalTime(21, 0)
    }
}
