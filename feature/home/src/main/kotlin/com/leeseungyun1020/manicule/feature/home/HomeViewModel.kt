package com.leeseungyun1020.manicule.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.home.HomeData
import com.leeseungyun1020.manicule.core.domain.home.ObserveHomeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val observeHomeData: ObserveHomeDataUseCase,
    ) : ViewModel() {
        private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        val uiState =
            retries
                .onStart { emit(Unit) }
                .flatMapLatest {
                    observeHomeData()
                        .map<HomeData, HomeUiState> { HomeUiState.Content(it) }
                        .onStart { emit(HomeUiState.Loading) }
                        .catch { emit(HomeUiState.Error) }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = HomeUiState.Loading,
                )

        fun retry() {
            retries.tryEmit(Unit)
        }
    }
