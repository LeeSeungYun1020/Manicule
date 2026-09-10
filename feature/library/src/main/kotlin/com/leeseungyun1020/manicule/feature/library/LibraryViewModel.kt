package com.leeseungyun1020.manicule.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryRoute
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryTab
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
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val initialStatus =
            LibraryTab.entries.firstOrNull { it.name == savedStateHandle.get<String>(SELECTED_TAB_KEY) }?.status
                ?: savedStateHandle.toRoute<LibraryRoute>().initialTab.status
        private val selectedStatus = MutableStateFlow(initialStatus)
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
                    initialValue = LibraryUiState.Loading(initialStatus),
                )

        fun selectStatus(status: ReadingStatus) {
            if (status == ReadingStatus.UNSET) return
            savedStateHandle[SELECTED_TAB_KEY] = status.name
            selectedStatus.value = status
        }

        fun retry() {
            retries.tryEmit(Unit)
        }

        private companion object {
            const val SELECTED_TAB_KEY = "librarySelectedTab"
        }
    }
