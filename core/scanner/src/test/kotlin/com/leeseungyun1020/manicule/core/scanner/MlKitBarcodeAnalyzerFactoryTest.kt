package com.leeseungyun1020.manicule.core.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.concurrent.Executor

@RunWith(JUnit4::class)
class MlKitBarcodeAnalyzerFactoryTest {
    private val directExecutor = Executor(Runnable::run)

    @Test
    fun rawValueIsDeliveredWithoutChanges() {
        val engine = FakeBarcodeAnalysisEngine()
        val results = mutableListOf<ScanResult>()
        createSession(engine, results::add)

        engine.emit(BarcodeAnalysisEvent.Detected(listOf(" 978-raw value ")))

        assertThat(results).containsExactly(ScanResult.Success(" 978-raw value "))
    }

    @Test
    fun nullValuesAreIgnoredUntilFirstNonNullValue() {
        val engine = FakeBarcodeAnalysisEngine()
        val results = mutableListOf<ScanResult>()
        createSession(engine, results::add)

        engine.emit(BarcodeAnalysisEvent.Detected(listOf(null, null)))
        engine.emit(BarcodeAnalysisEvent.Detected(listOf(null, "second", "third")))

        assertThat(results).containsExactly(ScanResult.Success("second"))
    }

    @Test
    fun resultsAfterFirstSuccessAreSuppressed() {
        val engine = FakeBarcodeAnalysisEngine()
        val results = mutableListOf<ScanResult>()
        createSession(engine, results::add)

        engine.emit(BarcodeAnalysisEvent.Detected(listOf("first")))
        engine.emit(BarcodeAnalysisEvent.Detected(listOf("duplicate")))
        engine.emit(BarcodeAnalysisEvent.Failed(IllegalStateException("late")))

        assertThat(results).containsExactly(ScanResult.Success("first"))
    }

    @Test
    fun newSessionCanDeliverSameValue() {
        val firstEngine = FakeBarcodeAnalysisEngine()
        val secondEngine = FakeBarcodeAnalysisEngine()
        val results = mutableListOf<ScanResult>()
        createSession(firstEngine, results::add)
        createSession(secondEngine, results::add)

        firstEngine.emit(BarcodeAnalysisEvent.Detected(listOf("same")))
        secondEngine.emit(BarcodeAnalysisEvent.Detected(listOf("same")))

        assertThat(results)
            .containsExactly(ScanResult.Success("same"), ScanResult.Success("same"))
            .inOrder()
    }

    @Test
    fun detectorFailureIsDelivered() {
        val engine = FakeBarcodeAnalysisEngine()
        val results = mutableListOf<ScanResult>()
        val cause = IllegalStateException("detector failed")
        createSession(engine, results::add)

        engine.emit(BarcodeAnalysisEvent.Failed(cause))

        assertThat(results).containsExactly(ScanResult.Failure(cause))
    }

    @Test
    fun closeReleasesEngineOnceAndSuppressesResults() {
        val engine = FakeBarcodeAnalysisEngine()
        val results = mutableListOf<ScanResult>()
        val session = createSession(engine, results::add)

        session.close()
        session.close()
        engine.emit(BarcodeAnalysisEvent.Detected(listOf("late")))

        assertThat(engine.closeCount).isEqualTo(1)
        assertThat(results).isEmpty()
    }

    private fun createSession(
        engine: FakeBarcodeAnalysisEngine,
        onResult: (ScanResult) -> Unit,
    ): BarcodeAnalyzerSession =
        MlKitBarcodeAnalyzerFactory(engineFactory = { engine })
            .create(directExecutor, onResult)
}

private class FakeBarcodeAnalysisEngine : BarcodeAnalysisEngine {
    private lateinit var onEvent: (BarcodeAnalysisEvent) -> Unit
    var closeCount: Int = 0
        private set

    override fun createAnalyzer(
        executor: Executor,
        onEvent: (BarcodeAnalysisEvent) -> Unit,
    ): ImageAnalysis.Analyzer {
        this.onEvent = onEvent
        return FakeAnalyzer
    }

    fun emit(event: BarcodeAnalysisEvent) {
        onEvent(event)
    }

    override fun close() {
        closeCount++
    }
}

private object FakeAnalyzer : ImageAnalysis.Analyzer {
    override fun analyze(image: ImageProxy) {
        image.close()
    }
}
