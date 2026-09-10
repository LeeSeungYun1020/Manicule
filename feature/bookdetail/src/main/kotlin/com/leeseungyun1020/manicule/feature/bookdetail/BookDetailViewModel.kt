package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.book.GetBookDetailUseCase
import com.leeseungyun1020.manicule.core.domain.library.ChangeReadingStatusUseCase
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
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
        private val changeStatus: ChangeReadingStatusUseCase,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val isbn: String = checkNotNull(savedStateHandle[ISBN_KEY])
        private val openMyRecords: Boolean = savedStateHandle[OPEN_MY_RECORDS_KEY] ?: false
        private var selectedTab: BookDetailTab? =
            restoredTab() ?: if (openMyRecords) BookDetailTab.MyRecords else null
        private var refreshStatus: RefreshStatus = RefreshStatus.Idle
        private var observationJob: Job? = null
        private var statusChange: StatusChangeState = StatusChangeState.Idle
        private var statusAttempt = 0L
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
                    .onSuccess { syncStatus ->
                        updateRefreshStatus(
                            status =
                                if (syncStatus == BookSyncStatus.COMPLETE) {
                                    RefreshStatus.Idle
                                } else {
                                    RefreshStatus.Failed
                                },
                            isFatalFailure = false,
                        )
                    }.onFailure {
                        updateRefreshStatus(
                            status = RefreshStatus.Failed,
                            isFatalFailure = true,
                        )
                    }
            }
        }

        fun changeReadingStatus(status: ReadingStatus) {
            if (_uiState.value !is BookDetailUiState.Content ||
                status == ReadingStatus.UNSET ||
                statusChange is StatusChangeState.Saving
            ) {
                return
            }
            val attempt = ++statusAttempt
            updateStatusChange(StatusChangeState.Saving(status))
            viewModelScope.launch {
                try {
                    val result = changeStatus(isbn, status)
                    updateStatusChange(
                        when (result) {
                            ReadingStatusChangeResult.Changed, ReadingStatusChangeResult.Unchanged -> StatusChangeState.Idle
                            ReadingStatusChangeResult.BookNotFound, ReadingStatusChangeResult.InvalidStatus -> StatusChangeState.Failed(
                                status,
                                attempt,
                            )
                        },
                    )
                } catch (cancelled: CancellationException) {
                    updateStatusChange(StatusChangeState.Idle)
                    throw cancelled
                } catch (_: Exception) {
                    updateStatusChange(StatusChangeState.Failed(status, attempt))
                }
            }
        }

        fun dismissStatusError() {
            if (statusChange is StatusChangeState.Failed) updateStatusChange(StatusChangeState.Idle)
        }

        private fun updateStatusChange(value: StatusChangeState) {
            statusChange = value
            _uiState.update { state ->
                if (state is BookDetailUiState.Content) state.copy(statusChange = value) else state
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
                                        statusChange = statusChange,
                                    )
                                } else {
                                    state
                                }
                            }
                        }
                }
        }

        private fun updateRefreshStatus(
            status: RefreshStatus,
            isFatalFailure: Boolean = false,
        ) {
            refreshStatus = status
            _uiState.update { state ->
                when {
                    state is BookDetailUiState.Content -> state.copy(refreshStatus = status)
                    status == RefreshStatus.Failed && isFatalFailure -> BookDetailUiState.Error
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
