package com.leeseungyun1020.manicule.core.scanner

sealed interface ScanResult {
    data class Success(
        val rawValue: String,
    ) : ScanResult

    data class Failure(
        val cause: Throwable,
    ) : ScanResult
}
