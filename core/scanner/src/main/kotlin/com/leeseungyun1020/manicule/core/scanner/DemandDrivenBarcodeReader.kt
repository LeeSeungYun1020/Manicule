package com.leeseungyun1020.manicule.core.scanner

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

internal sealed interface BarcodeDetectionEvent {
    data class Frame(
        val values: List<String>,
    ) : BarcodeDetectionEvent

    data class Failure(
        val cause: Throwable,
    ) : BarcodeDetectionEvent
}

internal fun interface BarcodeDetectionSource {
    fun detections(): Flow<BarcodeDetectionEvent>
}

internal class DemandDrivenBarcodeReader(
    source: BarcodeDetectionSource,
    private val sharingScope: CoroutineScope,
    private val release: () -> Unit,
) : AutoCloseable {
    private val lock = Any()
    private val requestJobs = mutableSetOf<Job>()
    private var closed = false

    private val detections =
        source
            .detections()
            .shareIn(
                scope = sharingScope,
                started = SharingStarted.WhileSubscribed(),
                replay = 0,
            )

    suspend fun getBarcodes(predicate: (String) -> Boolean = { true }): List<String> {
        synchronized(lock) {
            check(!closed) { "BarcodeReader is closed" }
        }

        return suspendCancellableCoroutine { continuation ->
            val request =
                sharingScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    val result =
                        runCatching {
                            detections
                                .map { event ->
                                    when (event) {
                                        is BarcodeDetectionEvent.Frame -> event.values.filter(predicate)
                                        is BarcodeDetectionEvent.Failure -> throw BarcodeAnalysisException(event.cause)
                                    }
                                }.first { it.isNotEmpty() }
                        }
                    completeContinuation(continuation, result)
                }

            synchronized(lock) {
                if (closed) {
                    request.cancel()
                    continuation.cancel(CancellationException("BarcodeReader is closed"))
                    return@suspendCancellableCoroutine
                }
                requestJobs += request
            }

            continuation.invokeOnCancellation {
                request.cancel()
                synchronized(lock) {
                    requestJobs -= request
                }
            }

            request.invokeOnCompletion {
                synchronized(lock) {
                    requestJobs -= request
                }
            }
        }
    }

    private fun completeContinuation(
        continuation: CancellableContinuation<List<String>>,
        result: Result<List<String>>,
    ) {
        synchronized(lock) {
            if (closed) {
                continuation.cancel(CancellationException("BarcodeReader is closed"))
            } else {
                continuation.resumeWith(result)
            }
        }
    }

    override fun close() {
        val jobs =
            synchronized(lock) {
                if (closed) return
                closed = true
                requestJobs.toList().also { requestJobs.clear() }
            }

        jobs.forEach { it.cancel(CancellationException("BarcodeReader is closed")) }
        sharingScope.cancel()
        release()
    }
}
