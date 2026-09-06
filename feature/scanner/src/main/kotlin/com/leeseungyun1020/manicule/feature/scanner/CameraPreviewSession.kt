package com.leeseungyun1020.manicule.feature.scanner

import kotlinx.coroutines.CancellationException

/** Main-thread owner of a single screen's binding, including pending initialization. */
internal class CameraPreviewSession(
    private val onReady: () -> Unit,
    private val onFailed: () -> Unit,
) : AutoCloseable {
    private var closed = false
    private var binding: PreviewBinding? = null

    // CameraX and device providers report different initialization exceptions at this boundary.
    @Suppress("TooGenericExceptionCaught")
    fun attach(createBinding: () -> PreviewBinding) {
        if (closed) return
        try {
            binding = createBinding()
            binding?.bind()
            if (!closed) onReady()
        } catch (exception: Exception) {
            fail(exception)
        }
    }

    fun fail(exception: Exception) {
        if (closed) return
        close()
        if (exception !is CancellationException) onFailed()
    }

    override fun close() {
        if (closed) return
        closed = true
        binding?.close()
        binding = null
    }
}

internal interface PreviewBinding : AutoCloseable {
    fun bind()
}
