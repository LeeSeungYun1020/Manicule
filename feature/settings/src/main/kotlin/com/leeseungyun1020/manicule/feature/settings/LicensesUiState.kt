package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.runtime.Immutable

@Immutable
sealed interface LicensesUiState {
    data object Loading : LicensesUiState

    data class Success(
        val libraries: List<OpenSourceLibrary>,
        val licenseText: String,
    ) : LicensesUiState

    data object Error : LicensesUiState
}
