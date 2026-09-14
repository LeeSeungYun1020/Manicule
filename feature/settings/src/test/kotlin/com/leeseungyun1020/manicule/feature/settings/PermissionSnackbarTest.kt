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
    fun dismissingRecovery_doesNotRunAction() =
        runTest {
            val host = SnackbarHostState()
            val snackbar = PermissionSnackbar(backgroundScope, host)
            var actions = 0
            snackbar.show("Permission required", actionLabel = "Open settings") { actions++ }
            runCurrent()
            val displayed = checkNotNull(host.currentSnackbarData)
            assertThat(displayed.visuals.actionLabel).isEqualTo("Open settings")
            assertThat(displayed.visuals.withDismissAction).isTrue()
            displayed.dismiss()
            runCurrent()

            assertThat(actions).isEqualTo(0)
            assertThat(host.currentSnackbarData).isNull()
        }

    @Test
    fun recoveryAction_canShowFailureFeedback() =
        runTest {
            val host = SnackbarHostState()
            val snackbar = PermissionSnackbar(backgroundScope, host)
            var actions = 0
            snackbar.show("Permission required", actionLabel = "Open settings") {
                actions++
                snackbar.show("Settings unavailable")
            }
            runCurrent()
            assertThat(actions).isEqualTo(0)
            checkNotNull(host.currentSnackbarData).performAction()
            runCurrent()

            assertThat(actions).isEqualTo(1)
            assertThat(host.currentSnackbarData?.visuals?.message).isEqualTo("Settings unavailable")
        }

    @Test
    fun repeatedRequests_showOneMessageWithoutSettingsAction() =
        runTest {
            val host = SnackbarHostState()
            val snackbar = PermissionSnackbar(backgroundScope, host)

            repeat(5) { snackbar.show("Permission required") }
            runCurrent()
            val displayed = checkNotNull(host.currentSnackbarData)
            repeat(5) { snackbar.show("Permission required") }
            runCurrent()
            assertThat(host.currentSnackbarData).isSameInstanceAs(displayed)

            assertThat(displayed.visuals.actionLabel).isNull()
            assertThat(displayed.visuals.duration).isEqualTo(SnackbarDuration.Short)
            displayed.dismiss()
            runCurrent()

            assertThat(host.currentSnackbarData).isNull()
        }

    @Test
    fun requestsWhileAnotherSnackbarIsVisible_queueOnlyOnePermissionMessage() =
        runTest {
            val host = SnackbarHostState()
            val snackbar = PermissionSnackbar(backgroundScope, host)
            backgroundScope.launch { host.showSnackbar("Update failed", duration = SnackbarDuration.Indefinite) }
            runCurrent()

            repeat(5) { snackbar.show("Permission required") }
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
            snackbar.show("Permission required")
            runCurrent()
            checkNotNull(host.currentSnackbarData).dismiss()
            runCurrent()

            snackbar.show("Permission required")
            runCurrent()
            checkNotNull(host.currentSnackbarData).dismiss()
            runCurrent()
            assertThat(host.currentSnackbarData).isNull()
        }
}
