package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.book.GetBookDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val isbn: String = checkNotNull(savedStateHandle[ISBN_KEY])
        private val openMyRecords: Boolean = savedStateHandle[OPEN_MY_RECORDS_KEY] ?: false
        private var selectedTab: BookDetailTab? =
            restoredTab() ?: if (openMyRecords) BookDetailTab.MyRecords else null
        private var refreshStatus: RefreshStatus = RefreshStatus.Idle
        private var observationJob: Job? = null
        private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
        val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

        init {
            retry()
        }

        fun selectTab(tab: BookDetailTab) {
            selectedTab = tab
            savedStateHandle[SELECTED_TAB_KEY] = tab.name
            _uiState.update { state ->
                if (state is BookDetailUiState.Content) {
                    state.copy(selectedTab = tab)
                } else {
                    state
                }
            }
        }

        fun retry() {
            observeBookDetail()

            if (refreshStatus == RefreshStatus.Refreshing) return

            updateRefreshStatus(RefreshStatus.Refreshing)
            viewModelScope.launch {
                getBookDetail
                    .refresh(isbn)
                    .onSuccess { updateRefreshStatus(RefreshStatus.Idle) }
                    .onFailure { updateRefreshStatus(RefreshStatus.Failed) }
            }
        }

        private fun observeBookDetail() {
            if (observationJob?.isActive == true) return

            observationJob =
                viewModelScope.launch {
                    getBookDetail(isbn)
                        .catch { _uiState.value = BookDetailUiState.Error }
                        .collect { bookDetail ->
                            _uiState.update { state ->
                                if (bookDetail != null) {
                                    val tab = selectedTab ?: initialTab(bookDetail.entry != null)
                                    selectedTab = tab
                                    BookDetailUiState.Content(
                                        bookDetail = bookDetail,
                                        selectedTab = tab,
                                        refreshStatus = refreshStatus,
                                    )
                                } else {
                                    state
                                }
                            }
                        }
                }
        }

        private fun updateRefreshStatus(status: RefreshStatus) {
            refreshStatus = status
            _uiState.update { state ->
                when {
                    state is BookDetailUiState.Content -> state.copy(refreshStatus = status)
                    status == RefreshStatus.Failed -> BookDetailUiState.Error
                    status == RefreshStatus.Refreshing -> BookDetailUiState.Loading
                    else -> state
                }
            }
        }

        private fun initialTab(isRegistered: Boolean): BookDetailTab =
            if (isRegistered) BookDetailTab.MyRecords else BookDetailTab.Information

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
