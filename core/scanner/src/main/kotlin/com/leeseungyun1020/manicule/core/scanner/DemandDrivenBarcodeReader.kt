package com.leeseungyun1020.manicule.core.scanner

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.job

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
        val requestJob = currentCoroutineContext().job
        synchronized(lock) {
            check(!closed) { "BarcodeReader is closed" }
            requestJobs += requestJob
        }

        return try {
            detections
                .map { event ->
                    when (event) {
                        is BarcodeDetectionEvent.Frame -> event.values.filter(predicate)
                        is BarcodeDetectionEvent.Failure -> throw BarcodeAnalysisException(event.cause)
                    }
                }.first { it.isNotEmpty() }
        } finally {
            synchronized(lock) {
                requestJobs -= requestJob
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
