package com.leeseungyun1020.manicule.core.scanner

import androidx.camera.core.ImageAnalysis

interface BarcodeReader : AutoCloseable {
    val imageAnalysis: ImageAnalysis

    suspend fun getBarcodes(predicate: (String) -> Boolean = { true }): List<String>
}
