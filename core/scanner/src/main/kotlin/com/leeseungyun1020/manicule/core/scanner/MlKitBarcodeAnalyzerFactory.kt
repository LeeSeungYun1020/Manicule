package com.leeseungyun1020.manicule.core.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

class MlKitBarcodeAnalyzerFactory internal constructor(
    private val engineFactory: () -> BarcodeAnalysisEngine,
) : BarcodeAnalyzerFactory {
    constructor() : this(engineFactory = ::MlKitBarcodeAnalysisEngine)

    override fun create(executor: Executor): BarcodeAnalyzerSession =
        MlKitBarcodeAnalyzerSession(
            engine = engineFactory(),
            executor = executor,
        )
}

private class MlKitBarcodeAnalyzerSession(
    private val engine: BarcodeAnalysisEngine,
    executor: Executor,
) : BarcodeAnalyzerSession {
    private val closed = AtomicBoolean(false)
    override val analyzer: ImageAnalysis.Analyzer = engine.createAnalyzer(executor)

    override suspend fun getBarcodes(predicate: (String) -> Boolean): List<String> {
        while (true) {
            val barcodes = engine.results.receive().getOrThrow().filter(predicate)
            if (barcodes.isNotEmpty()) return barcodes
        }
    }

    override fun close() {
        if (closed.compareAndSet(false, true)) {
            engine.close()
        }
    }
}

internal interface BarcodeAnalysisEngine : AutoCloseable {
    val results: ReceiveChannel<Result<List<String>>>

    fun createAnalyzer(executor: Executor): ImageAnalysis.Analyzer
}

private class MlKitBarcodeAnalysisEngine(
    private val scanner: BarcodeScanner = BarcodeScanning.getClient(),
) : BarcodeAnalysisEngine {
    private val _results = Channel<Result<List<String>>>(capacity = Channel.RENDEZVOUS)
    override val results: ReceiveChannel<Result<List<String>>> = _results

    override fun createAnalyzer(executor: Executor): ImageAnalysis.Analyzer =
        MlKitAnalyzer(
            listOf(scanner),
            ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
            executor,
        ) { result ->
            val cause = result.getThrowable(scanner)
            val analysisResult =
                if (cause != null) {
                    Result.failure(BarcodeAnalysisException(cause))
                } else {
                    Result.success(result.getValue(scanner).orEmpty().mapNotNull { it.rawValue })
                }
            _results.trySend(analysisResult)
        }

    override fun close() {
        _results.cancel(CancellationException("Barcode analyzer closed"))
        scanner.close()
    }
}
