package com.leeseungyun1020.manicule.core.scanner

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class DemandDrivenBarcodeReaderTest {
    @Test
    fun analysisStartsOnFirstRequestAndStopsAfterResult() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            val result = async(UnconfinedTestDispatcher(testScheduler)) { fixture.reader.getBarcodes() }

            runCurrent()
            assertThat(source.startCount).isEqualTo(1)
            assertThat(source.activeCount).isEqualTo(1)

            source.emitFrame("978-raw")

            assertThat(result.await()).containsExactly("978-raw")
            runCurrent()
            assertThat(source.activeCount).isEqualTo(0)
            assertThat(source.stopCount).isEqualTo(1)
            fixture.reader.close()
        }

    @Test
    fun concurrentRequestsShareAnalysisUntilLastResult() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            val isbn =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    fixture.reader.getBarcodes { it.startsWith("978") }
                }
            val other =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    fixture.reader.getBarcodes { it.startsWith("123") }
                }

            runCurrent()
            assertThat(source.startCount).isEqualTo(1)

            source.emitFrame("978-first", "other")

            assertThat(isbn.await()).containsExactly("978-first")
            runCurrent()
            assertThat(other.isCompleted).isFalse()
            assertThat(source.activeCount).isEqualTo(1)

            source.emitFrame("123-second")

            assertThat(other.await()).containsExactly("123-second")
            runCurrent()
            assertThat(source.activeCount).isEqualTo(0)
            assertThat(source.stopCount).isEqualTo(1)
            fixture.reader.close()
        }

    @Test
    fun concurrentRequestsWithQueuedDispatcherSubscribeImmediatelyAndShareSameFrame() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            val queuedDispatcher = kotlinx.coroutines.test.StandardTestDispatcher(testScheduler)

            val first = async(queuedDispatcher) { fixture.reader.getBarcodes { it.startsWith("978") } }
            val second = async(queuedDispatcher) { fixture.reader.getBarcodes { it.startsWith("978") } }

            runCurrent()
            source.emitFrame("978-shared")

            assertThat(first.await()).containsExactly("978-shared")
            assertThat(second.await()).containsExactly("978-shared")
            fixture.reader.close()
        }

    @Test
    fun cancellingLastRequestStopsAnalysis() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            val result = async(UnconfinedTestDispatcher(testScheduler)) { fixture.reader.getBarcodes() }
            runCurrent()

            result.cancel()
            runCurrent()

            assertThat(source.activeCount).isEqualTo(0)
            assertThat(source.stopCount).isEqualTo(1)
            fixture.reader.close()
        }

    @Test
    fun nextRequestRestartsWithoutReplayingOldResult() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            val first = async(UnconfinedTestDispatcher(testScheduler)) { fixture.reader.getBarcodes() }
            runCurrent()
            source.emitFrame("first")
            assertThat(first.await()).containsExactly("first")
            runCurrent()

            source.emitFrame("between")
            val second = async(UnconfinedTestDispatcher(testScheduler)) { fixture.reader.getBarcodes() }
            runCurrent()

            assertThat(source.startCount).isEqualTo(2)
            assertThat(second.isCompleted).isFalse()

            source.emitFrame("second")

            assertThat(second.await()).containsExactly("second")
            fixture.reader.close()
        }

    @Test
    fun failureIsBroadcastAndNextRequestRestarts() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            val cause = IllegalStateException("detector failed")

            supervisorScope {
                val first = async(UnconfinedTestDispatcher(testScheduler)) { fixture.reader.getBarcodes() }
                val second = async(UnconfinedTestDispatcher(testScheduler)) { fixture.reader.getBarcodes() }
                runCurrent()
                source.fail(cause)

                listOf(first, second).forEach { request ->
                    val exception = runCatching { request.await() }.exceptionOrNull()
                    assertThat(exception).isInstanceOf(BarcodeAnalysisException::class.java)
                    assertThat(generateSequence(exception) { it.cause }.last()).isSameInstanceAs(cause)
                }
            }
            runCurrent()

            val restarted = async(UnconfinedTestDispatcher(testScheduler)) { fixture.reader.getBarcodes() }
            runCurrent()
            assertThat(source.startCount).isEqualTo(2)
            source.emitFrame("restarted")
            assertThat(restarted.await()).containsExactly("restarted")
            fixture.reader.close()
        }

    @Test
    fun rawValuesAndPredicateArePreserved() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            val result =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    fixture.reader.getBarcodes { it.startsWith("978") }
                }
            runCurrent()

            source.emitFrame("123")
            source.emitFrame(" 978-not-matching", "978-first", "invalid", "978-second")

            assertThat(result.await()).containsExactly("978-first", "978-second").inOrder()
            fixture.reader.close()
        }

    @Test
    fun closeCancelsPendingRequestsAndReleasesOnce() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            val result = async(UnconfinedTestDispatcher(testScheduler)) { fixture.reader.getBarcodes() }
            runCurrent()

            fixture.reader.close()
            fixture.reader.close()
            runCurrent()

            assertThat(result.isCancelled).isTrue()
            assertThat(fixture.release.count).isEqualTo(1)
            val exception = runCatching { fixture.reader.getBarcodes() }.exceptionOrNull()
            assertThat(exception).isInstanceOf(IllegalStateException::class.java)
        }

    @Test
    fun closeDuringResultReturnThrowsCancellationException() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)

            val result =
                async(UnconfinedTestDispatcher(testScheduler)) {
                    fixture.reader.getBarcodes {
                        fixture.reader.close()
                        true
                    }
                }

            runCurrent()
            source.emitFrame("978-race")
            runCurrent()

            val exception = runCatching { result.await() }.exceptionOrNull()
            assertThat(exception).isInstanceOf(CancellationException::class.java)
            assertThat(exception?.message).isEqualTo("BarcodeReader is closed")
        }

    @Test
    fun closeCancelsOnlyPendingRequestAndPreservesCallerScopeAndSiblings() =
        runTest {
            val source = FakeBarcodeDetectionSource()
            val fixture = createFixture(source)
            var siblingCompleted = false
            var afterSuspend = false
            var caughtException: Throwable? = null

            val job =
                launch(UnconfinedTestDispatcher(testScheduler)) {
                    launch {
                        delay(100)
                        siblingCompleted = true
                    }

                    caughtException = runCatching { fixture.reader.getBarcodes() }.exceptionOrNull()
                    afterSuspend = true
                    delay(10)
                }

            runCurrent()
            fixture.reader.close()

            testScheduler.advanceTimeBy(100)
            runCurrent()

            assertThat(caughtException).isInstanceOf(CancellationException::class.java)
            assertThat(afterSuspend).isTrue()
            assertThat(siblingCompleted).isTrue()
        }

    private fun kotlinx.coroutines.test.TestScope.createFixture(source: FakeBarcodeDetectionSource): ReaderFixture {
        val release = FakeRelease()
        val scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher(testScheduler))
        return ReaderFixture(
            reader = DemandDrivenBarcodeReader(source, scope, release::invoke),
            release = release,
        )
    }
}

private data class ReaderFixture(
    val reader: DemandDrivenBarcodeReader,
    val release: FakeRelease,
)

private class FakeRelease {
    var count = 0
        private set

    fun invoke() {
        count++
    }
}

private class FakeBarcodeDetectionSource : BarcodeDetectionSource {
    private val events = MutableSharedFlow<BarcodeDetectionEvent>(extraBufferCapacity = 16)

    var startCount = 0
        private set
    var stopCount = 0
        private set
    var activeCount = 0
        private set

    override fun detections(): Flow<BarcodeDetectionEvent> =
        flow {
            startCount++
            activeCount++
            try {
                events.collect(::emit)
            } finally {
                activeCount--
                stopCount++
            }
        }

    fun emitFrame(vararg values: String) {
        check(events.tryEmit(BarcodeDetectionEvent.Frame(values.toList())))
    }

    fun fail(cause: Throwable) {
        check(events.tryEmit(BarcodeDetectionEvent.Failure(cause)))
    }
}
