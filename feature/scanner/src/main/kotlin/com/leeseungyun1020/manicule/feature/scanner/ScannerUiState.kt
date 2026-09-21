package com.leeseungyun1020.manicule.feature.scanner

sealed interface ScannerUiState {
    data class PermissionDenied(
        val requiresSettings: Boolean = false,
        val launchFailed: Boolean = false,
    ) : ScannerUiState

    data object Initializing : ScannerUiState

    /** Preview is bound and one barcode read is pending. */
    data object Scanning : ScannerUiState

    data object LookingUp : ScannerUiState

    data class Success(
        val isbn: String,
    ) : ScannerUiState

    data object NavigationDelivered : ScannerUiState

    data object Failed : ScannerUiState
}
