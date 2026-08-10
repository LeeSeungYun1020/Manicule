package com.leeseungyun1020.manicule.core.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.Executor

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(JUnit4::class)
class MlKitBarcodeAnalyzerFactoryTest {
    private val directExecutor = Executor(Runnable::run)

    @Test
    fun rawValuesAreDeliveredWithoutChanges() =
        runTest {
            val engine = FakeBarcodeAnalysisEngine()
            val session = createSession(engine)
            val deferred = async(UnconfinedTestDispatcher(testScheduler)) {
                session.getBarcodes()
            }

            engine.emit(listOf(" 978-raw value ", "second"))

            assertThat(deferred.await()).containsExactly(" 978-raw value ", "second").inOrder()
        }

    @Test
    fun allValuesSatisfyingPredicateInSameFrameAreDelivered() =
        runTest {
            val engine = FakeBarcodeAnalysisEngine()
            val session = createSession(engine)
            val deferred = async(UnconfinedTestDispatcher(testScheduler)) {
                session.getBarcodes { it.startsWith("978") }
            }

            engine.emit(listOf("12345"))
            engine.emit(listOf("978-first", "invalid", "978-second"))

            assertThat(deferred.await()).containsExactly("978-first", "978-second").inOrder()
        }

    @Test
    fun resultBeforeGetBarcodesIsIgnored() =
        runTest {
            val engine = FakeBarcodeAnalysisEngine()
            val session = createSession(engine)

            engine.emit(listOf("before"))

            val deferred = async(UnconfinedTestDispatcher(testScheduler)) {
                session.getBarcodes()
            }
            engine.emit(listOf("after"))

            assertThat(deferred.await()).containsExactly("after")
        }

    @Test
    fun detectorFailureIsPropagated() =
        runTest {
            val engine = FakeBarcodeAnalysisEngine()
            val session = createSession(engine)
            val cause = IllegalStateException("detector failed")

            val exception =
                supervisorScope {
                    val deferred = async(UnconfinedTestDispatcher(testScheduler)) {
                        session.getBarcodes()
                    }
                    engine.fail(cause)

                    runCatching { deferred.await() }.exceptionOrNull()
                }
            assertThat(exception).isInstanceOf(BarcodeAnalysisException::class.java)
            assertThat(generateSequence(exception) { it.cause }.last()).isSameInstanceAs(cause)
        }

    @Test
    fun closeReleasesEngineOnceAndCancelsPendingRequest() =
        runTest {
            val engine = FakeBarcodeAnalysisEngine()
            val session = createSession(engine)
            val deferred = async(UnconfinedTestDispatcher(testScheduler)) {
                session.getBarcodes()
            }

            session.close()
            session.close()

            assertThat(engine.closeCount).isEqualTo(1)
            assertThat(deferred.isCancelled).isTrue()
        }

    private fun createSession(engine: FakeBarcodeAnalysisEngine): BarcodeAnalyzerSession =
        MlKitBarcodeAnalyzerFactory(engineFactory = { engine })
            .create(directExecutor)
}

private class FakeBarcodeAnalysisEngine : BarcodeAnalysisEngine {
    private val _results = Channel<Result<List<String>>>(capacity = Channel.RENDEZVOUS)
    override val results: ReceiveChannel<Result<List<String>>> = _results
    var closeCount: Int = 0
        private set

    override fun createAnalyzer(executor: Executor): ImageAnalysis.Analyzer = FakeAnalyzer

    fun emit(values: List<String?>) {
        _results.trySend(Result.success(values.filterNotNull()))
    }

    fun fail(cause: Throwable) {
        _results.trySend(Result.failure(BarcodeAnalysisException(cause)))
    }

    override fun close() {
        closeCount++
        _results.cancel(CancellationException("Barcode analyzer closed"))
    }
}

private object FakeAnalyzer : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        image.close()
    }
}
