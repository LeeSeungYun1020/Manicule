package com.leeseungyun1020.manicule.core.notifications

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.domain.settings.ReminderContent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ReminderWorkerTest {
    @Test
    fun successfulContent_isPublishedAndCompletes() =
        runTest {
            var published: ReminderContent? = null

            val completed =
                runReminder(
                    getContent = { ReminderContent.Book("Book") },
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
                    getContent = { error("temporary") },
                    publish = {},
                )

            assertThat(completed).isFalse()
        }

    @Test
    fun notificationPostingError_doesNotRetryContentWork() =
        runTest {
            val completed =
                runReminder(
                    getContent = { ReminderContent.Generic },
                    publish = { error("notifications unavailable") },
                )

            assertThat(completed).isTrue()
        }
}
