package com.leeseungyun1020.manicule.core.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class MlKitBarcodeAnalyzerFactory internal constructor(
    private val engineFactory: () -> BarcodeAnalysisEngine,
) : BarcodeAnalyzerFactory {
    constructor() : this(engineFactory = ::MlKitBarcodeAnalysisEngine)

    override fun create(
        executor: Executor,
        onResult: (ScanResult) -> Unit,
    ): BarcodeAnalyzerSession =
        MlKitBarcodeAnalyzerSession(
            engine = engineFactory(),
            executor = executor,
            onResult = onResult,
        )
}

private class MlKitBarcodeAnalyzerSession(
    private val engine: BarcodeAnalysisEngine,
    executor: Executor,
    private val onResult: (ScanResult) -> Unit,
) : BarcodeAnalyzerSession {
    private val closed = AtomicBoolean(false)
    private val successDelivered = AtomicBoolean(false)

    override val analyzer: ImageAnalysis.Analyzer =
        engine.createAnalyzer(executor) { event ->
            if (closed.get() || successDelivered.get()) return@createAnalyzer

            when (event) {
                is BarcodeAnalysisEvent.Detected -> {
                    val rawValue = event.rawValues.firstNotNullOfOrNull { it }
                    if (rawValue != null && successDelivered.compareAndSet(false, true)) {
                        onResult(ScanResult.Success(rawValue))
                    }
                }

                is BarcodeAnalysisEvent.Failed -> onResult(ScanResult.Failure(event.cause))
            }
        }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            engine.close()
        }
    }
}

internal sealed interface BarcodeAnalysisEvent {
    data class Detected(
        val rawValues: List<String?>,
    ) : BarcodeAnalysisEvent

    data class Failed(
        val cause: Throwable,
    ) : BarcodeAnalysisEvent
}

internal interface BarcodeAnalysisEngine : AutoCloseable {
    fun createAnalyzer(
        executor: Executor,
        onEvent: (BarcodeAnalysisEvent) -> Unit,
    ): ImageAnalysis.Analyzer
}

private class MlKitBarcodeAnalysisEngine(
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(),
) : BarcodeAnalysisEngine {
    override fun createAnalyzer(
        executor: Executor,
        onEvent: (BarcodeAnalysisEvent) -> Unit,
    ): ImageAnalysis.Analyzer =
        MlKitAnalyzer(
            listOf(scanner),
            ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
            executor,
        ) { result ->
            val cause = result.getThrowable(scanner)
            if (cause != null) {
                onEvent(BarcodeAnalysisEvent.Failed(cause))
            } else {
                onEvent(
                    BarcodeAnalysisEvent.Detected(
                        result.getValue(scanner).orEmpty().map { it.rawValue },
                    ),
                )
            }
        }

    override fun close() {
        scanner.close()
    }
}
