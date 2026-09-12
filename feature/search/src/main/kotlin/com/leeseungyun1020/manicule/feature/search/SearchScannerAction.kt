package com.leeseungyun1020.manicule.feature.search

sealed interface SearchScannerAction {
    data object Unavailable : SearchScannerAction

    data class Available(
        val onNavigate: () -> Unit,
    ) : SearchScannerAction
}
