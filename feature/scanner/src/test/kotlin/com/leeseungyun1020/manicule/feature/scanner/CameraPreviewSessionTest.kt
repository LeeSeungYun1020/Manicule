package com.leeseungyun1020.manicule.feature.scanner

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class CameraPreviewSessionTest {
    private var ready = 0
    private var failed = 0
    private val session = CameraPreviewSession(onReady = { ready++ }, onFailed = { failed++ })

    @Test
    fun bindsAndClosesOnlyOwnedBindingOnce() {
        val owned = FakeBinding()
        val other = FakeBinding()
        session.attach { owned }
        assertThat(ready).isEqualTo(1)
        assertThat(owned.binds).isEqualTo(1)
        session.close()
        session.close()
        assertThat(owned.closes).isEqualTo(1)
        assertThat(other.closes).isEqualTo(0)
    }

    @Test
    fun lateProviderAfterScreenExitDoesNotCreateBindOrUpdateState() {
        session.close()
        session.attach { error("Late provider must not be accessed") }
        session.fail(IllegalStateException())
        assertThat(ready).isEqualTo(0)
        assertThat(failed).isEqualTo(0)
    }

    @Test
    fun providerInitializationFailureShowsFailure() {
        session.attach { throw IllegalStateException("provider unavailable") }
        assertThat(failed).isEqualTo(1)
        assertThat(ready).isEqualTo(0)
    }

    @Test
    fun bindingFailureReleasesPartialBindingAndShowsFailure() {
        val binding = FakeBinding(IllegalArgumentException("no rear camera"))
        session.attach { binding }
        assertThat(binding.closes).isEqualTo(1)
        assertThat(failed).isEqualTo(1)
        assertThat(ready).isEqualTo(0)
    }

    @Test
    fun lifecycleCancellationDoesNotShowFailure() {
        val binding = FakeBinding(CancellationException())
        session.attach { binding }
        assertThat(binding.closes).isEqualTo(1)
        assertThat(failed).isEqualTo(0)
    }

    private class FakeBinding(
        private val failure: Exception? = null,
    ) : PreviewBinding {
        var binds = 0
        var closes = 0

        override fun bind() {
            binds++
            failure?.let { throw it }
        }

        override fun close() {
            closes++
        }
    }
}
