package com.leeseungyun1020.manicule.feature.scanner

sealed interface ScannerUiState {
    data class PermissionDenied(
        val requiresSettings: Boolean = false,
        val launchFailed: Boolean = false,
    ) : ScannerUiState

    data object Initializing : ScannerUiState

    /** Preview is bound; barcode recognition is not started yet. */
    data object Scanning : ScannerUiState

    data object Failed : ScannerUiState
}
