package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PermissionSnackbarTest {
    @Test
    fun repeatedRequests_openSettingsOnceWithoutQueuedDuplicates() =
        runTest {
            val host = SnackbarHostState()
            val snackbar = PermissionSnackbar(backgroundScope, host)
            var opened = 0

            repeat(5) { snackbar.show("Permission required", "Open settings") { opened++ } }
            runCurrent()
            val displayed = checkNotNull(host.currentSnackbarData)
            repeat(5) { snackbar.show("Permission required", "Open settings") { opened++ } }
            runCurrent()
            assertThat(host.currentSnackbarData).isSameInstanceAs(displayed)

            displayed.performAction()
            runCurrent()

            assertThat(opened).isEqualTo(1)
            assertThat(host.currentSnackbarData).isNull()
        }

    @Test
    fun requestsWhileAnotherSnackbarIsVisible_queueOnlyOnePermissionMessage() =
        runTest {
            val host = SnackbarHostState()
            val snackbar = PermissionSnackbar(backgroundScope, host)
            backgroundScope.launch { host.showSnackbar("Update failed", duration = SnackbarDuration.Indefinite) }
            runCurrent()

            repeat(5) { snackbar.show("Permission required", "Open settings") {} }
            runCurrent()
            assertThat(host.currentSnackbarData?.visuals?.message).isEqualTo("Update failed")
            checkNotNull(host.currentSnackbarData).dismiss()
            runCurrent()

            assertThat(host.currentSnackbarData?.visuals?.message).isEqualTo("Permission required")
            checkNotNull(host.currentSnackbarData).dismiss()
            runCurrent()
            assertThat(host.currentSnackbarData).isNull()
        }

    @Test
    fun dismissedMessage_allowsAnotherRequest() =
        runTest {
            val host = SnackbarHostState()
            val snackbar = PermissionSnackbar(backgroundScope, host)
            var opened = 0
            snackbar.show("Permission required", "Open settings") { opened++ }
            runCurrent()
            checkNotNull(host.currentSnackbarData).dismiss()
            runCurrent()
            assertThat(opened).isEqualTo(0)

            snackbar.show("Permission required", "Open settings") { opened++ }
            runCurrent()
            checkNotNull(host.currentSnackbarData).performAction()
            runCurrent()
            assertThat(opened).isEqualTo(1)
            assertThat(host.currentSnackbarData).isNull()
        }
}
