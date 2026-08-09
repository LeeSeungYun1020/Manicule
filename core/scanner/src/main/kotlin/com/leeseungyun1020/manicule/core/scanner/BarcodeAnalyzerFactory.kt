package com.leeseungyun1020.manicule.core.scanner

import java.util.concurrent.Executor

fun interface BarcodeAnalyzerFactory {
    fun create(
        executor: Executor,
        onResult: (ScanResult) -> Unit,
    ): BarcodeAnalyzerSession
}
