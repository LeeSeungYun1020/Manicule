package com.leeseungyun1020.manicule.core.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import com.google.mlkit.vision.barcode.BarcodeScanning
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import java.util.concurrent.Executor

class MlKitBarcodeReaderFactory : BarcodeReaderFactory {
    override fun create(executor: Executor): BarcodeReader {
        val imageAnalysis =
            ImageAnalysis
                .Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
        val scanner = BarcodeScanning.getClient()
        val sharingScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        val source =
            BarcodeDetectionSource {
                callbackFlow {
                    val analyzer =
                        MlKitAnalyzer(
                            listOf(scanner),
                            ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                            executor,
                        ) { result ->
                            val event =
                                result.getThrowable(scanner)?.let(BarcodeDetectionEvent::Failure)
                                    ?: BarcodeDetectionEvent.Frame(
                                        result
                                            .getValue(scanner)
                                            .orEmpty()
                                            .mapNotNull { it.rawValue },
                                    )
                            trySend(event)
                        }
                    imageAnalysis.setAnalyzer(executor, analyzer)
                    awaitClose { imageAnalysis.clearAnalyzer() }
                }.catch { emit(BarcodeDetectionEvent.Failure(it)) }
            }
        val delegate =
            DemandDrivenBarcodeReader(
                source = source,
                sharingScope = sharingScope,
                release = {
                    imageAnalysis.clearAnalyzer()
                    scanner.close()
                },
            )
        return MlKitBarcodeReader(
            imageAnalysis = imageAnalysis,
            delegate = delegate,
        )
    }
}

private class MlKitBarcodeReader(
    override val imageAnalysis: ImageAnalysis,
    private val delegate: DemandDrivenBarcodeReader,
) : BarcodeReader {
    override suspend fun getBarcodes(predicate: (String) -> Boolean): List<String> = delegate.getBarcodes(predicate)

    override fun close() {
        delegate.close()
    }
}
