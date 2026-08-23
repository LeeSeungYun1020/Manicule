package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.book.GetBookDetailUseCase
import com.leeseungyun1020.manicule.core.domain.library.ObserveBookEntryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BookDetailViewModel
    @Inject
    constructor(
        private val getBookDetail: GetBookDetailUseCase,
        private val observeBookEntry: ObserveBookEntryUseCase,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val isbn: String = checkNotNull(savedStateHandle[ISBN_KEY])
        private val openMyRecords: Boolean = savedStateHandle[OPEN_MY_RECORDS_KEY] ?: false
        private val _uiState =
            MutableStateFlow(
                BookDetailUiState(
                    selectedTab = restoredTab() ?: if (openMyRecords) BookDetailTab.MyRecords else BookDetailTab.Information,
                ),
            )
        val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

        private var refreshFinished = false
        private var refreshFailed = false
        private var initialTabResolved = restoredTab() != null || openMyRecords

        init {
            observeBook()
            resolveInitialTab()
            retry()
        }

        fun selectTab(tab: BookDetailTab) {
            initialTabResolved = true
            savedStateHandle[SELECTED_TAB_KEY] = tab.name
            _uiState.update { it.copy(selectedTab = tab) }
        }

        fun retry() {
            refreshFinished = false
            refreshFailed = false
            _uiState.update { it.copy(isLoading = it.book == null, isFatalError = false, isRefreshError = false) }
            viewModelScope.launch {
                val result = getBookDetail.refresh(isbn)
                refreshFinished = true
                refreshFailed = result.isFailure
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isFatalError = result.isFailure && state.book == null,
                        isRefreshError = result.isFailure && state.book != null,
                    )
                }
            }
        }

        private fun observeBook() {
            viewModelScope.launch {
                getBookDetail(isbn)
                    .catch {
                        refreshFinished = true
                        refreshFailed = true
                        _uiState.update { state -> state.copy(isLoading = false, isFatalError = state.book == null) }
                    }.collect { book ->
                        _uiState.update { state ->
                            state.copy(
                                book = book,
                                isLoading = book == null && !refreshFinished,
                                isFatalError = book == null && refreshFinished && refreshFailed,
                                isRefreshError = book != null && refreshFinished && refreshFailed,
                            )
                        }
                    }
            }
        }

        private fun resolveInitialTab() {
            if (initialTabResolved) return
            viewModelScope.launch {
                observeBookEntry(isbn).collect { entry ->
                    if (!initialTabResolved) {
                        initialTabResolved = true
                        if (entry != null) selectTab(BookDetailTab.MyRecords)
                    }
                }
            }
        }

        private fun restoredTab(): BookDetailTab? =
            savedStateHandle.get<String>(SELECTED_TAB_KEY)?.let { saved ->
                BookDetailTab.entries.firstOrNull { it.name == saved }
            }

        private companion object {
            const val ISBN_KEY = "isbn"
            const val OPEN_MY_RECORDS_KEY = "openMyRecords"
            const val SELECTED_TAB_KEY = "bookDetailSelectedTab"
        }
    }
