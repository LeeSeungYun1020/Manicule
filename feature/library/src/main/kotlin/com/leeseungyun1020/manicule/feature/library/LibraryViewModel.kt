package com.leeseungyun1020.manicule.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel
    @Inject
    constructor(
        getLibraryBooks: GetLibraryBooksUseCase,
    ) : ViewModel() {
        private val selectedStatus = MutableStateFlow(ReadingStatus.READING)
        private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        val uiState =
            combine(selectedStatus, retries.onStart { emit(Unit) }) { status, _ -> status }
                .flatMapLatest { status ->
                    getLibraryBooks(status)
                        .map<List<BookEntry>, LibraryUiState> { books ->
                            LibraryUiState.Content(status, books)
                        }.onStart { emit(LibraryUiState.Loading(status)) }
                        .catch { emit(LibraryUiState.Error(status)) }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5_000),
                    initialValue = LibraryUiState.Loading(ReadingStatus.READING),
                )

        fun selectStatus(status: ReadingStatus) {
            selectedStatus.value = status
        }

        fun retry() {
            retries.tryEmit(Unit)
        }
    }
