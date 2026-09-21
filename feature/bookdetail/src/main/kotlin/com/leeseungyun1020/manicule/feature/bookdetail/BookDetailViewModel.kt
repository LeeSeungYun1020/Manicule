package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.book.GetBookDetailUseCase
import com.leeseungyun1020.manicule.core.domain.library.ChangeReadingStatusUseCase
import com.leeseungyun1020.manicule.core.domain.record.AddReadingRecordUseCase
import com.leeseungyun1020.manicule.core.domain.record.ObserveBookRecordsUseCase
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject

private fun Flow<List<ReadingRecord>>.recoverRecordObservation() =
    onStart { emit(emptyList()) }
        .catch { cause ->
            if (cause is CancellationException) throw cause
        }

@HiltViewModel
class BookDetailViewModel
    @Inject
    constructor(
        private val getBookDetail: GetBookDetailUseCase,
        private val changeStatus: ChangeReadingStatusUseCase,
        private val observeBookRecords: ObserveBookRecordsUseCase,
        private val addReadingRecord: AddReadingRecordUseCase,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val isbn: String = checkNotNull(savedStateHandle[ISBN_KEY])
        private val openMyRecords: Boolean = savedStateHandle[OPEN_MY_RECORDS_KEY] ?: false
        private var selectedTab: BookDetailTab? =
            savedStateHandle.get<String>(SELECTED_TAB_KEY)?.let { saved ->
                BookDetailTab.entries.firstOrNull { it.name == saved }
            } ?: if (openMyRecords) BookDetailTab.MyRecords else null
        private var refreshStatus: RefreshStatus = RefreshStatus.Idle
        private var observationJob: Job? = null
        private var statusChange: StatusChangeState = StatusChangeState.Idle
        private var statusAttempt = 0L
        private var recordSaving: RecordSavingState = RecordSavingState.Idle
        private var recordAttempt = 0L
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

        fun addRecord(
            date: LocalDate,
            time: LocalTime,
            startPage: Int,
            endPage: Int,
        ) {
            if (_uiState.value !is BookDetailUiState.Content ||
                recordSaving is RecordSavingState.Saving
            ) {
                return
            }
            val attempt = ++recordAttempt
            updateRecordSaving(RecordSavingState.Saving(attempt))
            viewModelScope.launch {
                try {
                    addReadingRecord(
                        isbn = isbn,
                        date = date,
                        time = time,
                        startPage = startPage,
                        endPage = endPage,
                    )
                    updateRecordSaving(RecordSavingState.Succeeded(attempt))
                } catch (cancelled: CancellationException) {
                    updateRecordSaving(RecordSavingState.Idle)
                    throw cancelled
                } catch (_: Exception) {
                    updateRecordSaving(RecordSavingState.Failed(attempt))
                }
            }
        }

        fun dismissRecordError() {
            if (recordSaving is RecordSavingState.Failed) updateRecordSaving(RecordSavingState.Idle)
        }

        private fun updateRecordSaving(value: RecordSavingState) {
            recordSaving = value
            _uiState.update { state ->
                if (state is BookDetailUiState.Content) state.copy(recordSaving = value) else state
            }
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
                    combine(
                        getBookDetail(isbn),
                        observeBookRecords(isbn).recoverRecordObservation(),
                    ) { bookDetail, records -> bookDetail to records }
                        .catch { _uiState.value = BookDetailUiState.Error }
                        .collect { (bookDetail, records) ->
                            _uiState.update { state ->
                                if (bookDetail != null) {
                                    val tab =
                                        selectedTab ?: if (bookDetail.entry != null) {
                                            BookDetailTab.MyRecords
                                        } else {
                                            BookDetailTab.Information
                                        }
                                    selectedTab = tab
                                    BookDetailUiState.Content(
                                        bookDetail = bookDetail,
                                        records = records,
                                        selectedTab = tab,
                                        refreshStatus = refreshStatus,
                                        statusChange = statusChange,
                                        recordSaving = recordSaving,
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

        private companion object {
            const val ISBN_KEY = "isbn"
            const val OPEN_MY_RECORDS_KEY = "openMyRecords"
            const val SELECTED_TAB_KEY = "bookDetailSelectedTab"
        }
    }
