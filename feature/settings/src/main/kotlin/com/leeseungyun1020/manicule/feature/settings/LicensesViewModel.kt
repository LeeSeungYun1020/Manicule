package com.leeseungyun1020.manicule.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LicensesViewModel
    @Inject
    constructor(
        private val licenseLoader: OpenSourceLicenseLoader,
    ) : ViewModel() {

        private val _uiState = MutableStateFlow<LicensesUiState>(LicensesUiState.Loading)
        val uiState: StateFlow<LicensesUiState> = _uiState.asStateFlow()

        init {
            loadLicenses()
        }

        fun retry() {
            loadLicenses()
        }

        private fun loadLicenses() {
            _uiState.value = LicensesUiState.Loading
            viewModelScope.launch {
                try {
                    val libraries = licenseLoader.loadLibraries()
                    val licenseText = licenseLoader.loadLicenseText()
                    _uiState.value = LicensesUiState.Success(
                        libraries = libraries,
                        licenseText = licenseText,
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    _uiState.value = LicensesUiState.Error
                }
            }
        }
    }
