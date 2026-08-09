package com.leeseungyun1020.manicule.core.scanner

import androidx.camera.core.ImageAnalysis

interface BarcodeAnalyzerSession : AutoCloseable {
    val analyzer: ImageAnalysis.Analyzer
}
