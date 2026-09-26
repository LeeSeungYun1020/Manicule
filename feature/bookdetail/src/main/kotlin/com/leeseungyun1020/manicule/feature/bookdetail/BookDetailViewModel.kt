package com.leeseungyun1020.manicule.feature.bookdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leeseungyun1020.manicule.core.domain.book.GetBookDetailUseCase
import com.leeseungyun1020.manicule.core.domain.library.ChangeReadingStatusUseCase
import com.leeseungyun1020.manicule.core.domain.library.UpdateMemoUseCase
import com.leeseungyun1020.manicule.core.domain.library.UpdateRatingUseCase
import com.leeseungyun1020.manicule.core.domain.record.AddReadingRecordUseCase
import com.leeseungyun1020.manicule.core.domain.record.ObserveBookRecordsUseCase
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.model.MemoChangeResult
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject

private sealed interface RecordObservation {
    data class Loaded(
        val records: List<ReadingRecord>,
    ) : RecordObservation

    data class Failed(
        val attempt: Long,
    ) : RecordObservation
}

private fun ObserveBookRecordsUseCase.observeWithRetry(
    isbn: String,
    retrySignals: Flow<Long>,
): Flow<RecordObservation> =
    retrySignals.flatMapLatest { attempt ->
        this(isbn)
            .map<List<ReadingRecord>, RecordObservation>(RecordObservation::Loaded)
            .catch { emit(RecordObservation.Failed(attempt)) }
    }

private inline fun MutableStateFlow<BookDetailUiState>.updateContent(transform: (BookDetailUiState.Content) -> BookDetailUiState.Content) {
    update { state -> if (state is BookDetailUiState.Content) transform(state) else state }
}

private fun resolveRecordObservation(
    previousRecords: List<ReadingRecord>,
    observation: RecordObservation,
): Pair<List<ReadingRecord>, RecordLoadState> =
    when (observation) {
        is RecordObservation.Loaded -> observation.records to RecordLoadState.Idle
        is RecordObservation.Failed -> previousRecords to RecordLoadState.Failed(observation.attempt)
    }

private fun resolveRatingSaving(
    previousRatingSaving: RatingSavingState?,
    entryRating: Int,
): RatingSavingState =
    when (previousRatingSaving) {
        is RatingSavingState.Saving -> {
            if (entryRating == previousRatingSaving.target) {
                RatingSavingState.Idle
            } else {
                previousRatingSaving
            }
        }

        else -> previousRatingSaving ?: RatingSavingState.Idle
    }

private fun resolveMemoSaving(
    previousMemoSaving: MemoSavingState?,
    entryMemo: String?,
): MemoSavingState =
    when (previousMemoSaving) {
        is MemoSavingState.Saving -> {
            if (entryMemo == previousMemoSaving.target) {
                MemoSavingState.Idle
            } else {
                previousMemoSaving
            }
        }

        else -> previousMemoSaving ?: MemoSavingState.Idle
    }

@Suppress("TooManyFunctions", "LongParameterList")
@HiltViewModel
class BookDetailViewModel
    @Inject
    constructor(
        private val getBookDetail: GetBookDetailUseCase,
        private val changeStatus: ChangeReadingStatusUseCase,
        private val updateRatingUseCase: UpdateRatingUseCase,
        private val updateMemoUseCase: UpdateMemoUseCase,
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
        private var statusAttempt = 0L
        private var ratingAttempt = 0L
        private var memoAttempt = 0L
        private var memoSaveJob: Job? = null
        private var memoDraft: String? = savedStateHandle[MEMO_DRAFT_KEY]
        private var recordAttempt = 0L
        private val recordRetrySignals = MutableStateFlow(0L)
        private val _uiState = MutableStateFlow<BookDetailUiState>(BookDetailUiState.Loading)
        val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

        init {
            retry()
        }

        fun selectTab(tab: BookDetailTab) {
            selectedTab = tab
            savedStateHandle[SELECTED_TAB_KEY] = tab.name
            _uiState.updateContent { it.copy(selectedTab = tab) }
        }

        fun retry() {
            recordRetrySignals.update { it + 1 }
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
            val content = _uiState.value as? BookDetailUiState.Content ?: return
            if (status == ReadingStatus.UNSET || content.statusChange is StatusChangeState.Saving) return
            val attempt = ++statusAttempt
            _uiState.updateContent { it.copy(statusChange = StatusChangeState.Saving(status)) }
            viewModelScope.launch {
                runCatching { changeStatus(isbn, status) }
                    .onSuccess { result ->
                        _uiState.updateContent {
                            it.copy(
                                statusChange =
                                    when (result) {
                                        ReadingStatusChangeResult.Changed, ReadingStatusChangeResult.Unchanged -> StatusChangeState.Idle
                                        ReadingStatusChangeResult.BookNotFound, ReadingStatusChangeResult.InvalidStatus ->
                                            StatusChangeState.Failed(status, attempt)
                                    },
                            )
                        }
                    }.onFailure { e ->
                        if (e is CancellationException) {
                            _uiState.updateContent { it.copy(statusChange = StatusChangeState.Idle) }
                            throw e
                        }
                        _uiState.updateContent { it.copy(statusChange = StatusChangeState.Failed(status, attempt)) }
                    }
            }
        }

        fun dismissStatusError() {
            _uiState.updateContent {
                if (it.statusChange is StatusChangeState.Failed) it.copy(statusChange = StatusChangeState.Idle) else it
            }
        }

        fun updateRating(star: Int) {
            val content = _uiState.value as? BookDetailUiState.Content ?: return
            if (content.ratingSaving is RatingSavingState.Saving) return
            val currentRating = content.bookDetail.entry?.rating ?: 0
            val target = if (star == currentRating) 0 else star
            saveRating(target)
        }

        fun retryRating() {
            val content = _uiState.value as? BookDetailUiState.Content ?: return
            val failed = content.ratingSaving as? RatingSavingState.Failed ?: return
            saveRating(failed.target)
        }

        fun dismissRatingError() {
            _uiState.updateContent {
                if (it.ratingSaving is RatingSavingState.Failed) it.copy(ratingSaving = RatingSavingState.Idle) else it
            }
        }

        private fun saveRating(target: Int) {
            val attempt = ++ratingAttempt
            _uiState.updateContent { it.copy(ratingSaving = RatingSavingState.Saving(target)) }
            viewModelScope.launch {
                runCatching { updateRatingUseCase(isbn, target) }
                    .onSuccess { result ->
                        _uiState.updateContent { state ->
                            when (result) {
                                RatingChangeResult.Changed -> {
                                    val currentRating = state.bookDetail.entry?.rating ?: 0
                                    if (currentRating == target) {
                                        state.copy(ratingSaving = RatingSavingState.Idle)
                                    } else {
                                        state
                                    }
                                }

                                RatingChangeResult.Unchanged -> {
                                    state.copy(ratingSaving = RatingSavingState.Idle)
                                }

                                RatingChangeResult.BookNotFound, RatingChangeResult.InvalidRating -> {
                                    state.copy(ratingSaving = RatingSavingState.Failed(target, attempt))
                                }
                            }
                        }
                    }
                    .onFailure { e ->
                        if (e is CancellationException) {
                            _uiState.updateContent { it.copy(ratingSaving = RatingSavingState.Idle) }
                            throw e
                        }
                        _uiState.updateContent { it.copy(ratingSaving = RatingSavingState.Failed(target, attempt)) }
                    }
            }
        }

        fun updateMemoDraft(draft: String) {
            memoDraft = draft
            savedStateHandle[MEMO_DRAFT_KEY] = draft
            _uiState.updateContent { it.copy(memoDraft = draft) }
        }

        fun saveMemo() {
            val content = _uiState.value as? BookDetailUiState.Content ?: return
            if (content.memoSaving is MemoSavingState.Saving) return
            val draft = content.memoDraft ?: return
            val currentMemo = content.bookDetail.entry?.memo
            val normalizedDraft = draft.trim().ifEmpty { null }
            if (normalizedDraft == currentMemo) {
                clearMemoDraft()
                return
            }
            memoSaveJob =
                viewModelScope.launch {
                    saveMemoInternal(normalizedDraft)
                }
        }

        suspend fun saveMemoAndCheckSuccess(): Boolean {
            val activeJob = memoSaveJob
            if (activeJob != null && activeJob.isActive) {
                activeJob.join()
                val state = _uiState.value as? BookDetailUiState.Content
                return state?.memoSaving !is MemoSavingState.Failed
            }
            val content = _uiState.value as? BookDetailUiState.Content
            val draft = content?.memoDraft
            val normalizedDraft = draft?.trim()?.ifEmpty { null }
            val currentMemo = content?.bookDetail?.entry?.memo
            return when {
                draft == null -> true
                normalizedDraft == currentMemo -> {
                    clearMemoDraft()
                    true
                }

                else -> saveMemoInternal(normalizedDraft)
            }
        }

        fun retryMemo() {
            val content = _uiState.value as? BookDetailUiState.Content ?: return
            val failed = content.memoSaving as? MemoSavingState.Failed ?: return
            val currentDraft = content.memoDraft
            val normalizedDraft = currentDraft?.trim()?.ifEmpty { null }
            val targetToSave = normalizedDraft ?: failed.target
            memoSaveJob =
                viewModelScope.launch {
                    saveMemoInternal(targetToSave)
                }
        }

        fun dismissMemoError() {
            _uiState.updateContent {
                if (it.memoSaving is MemoSavingState.Failed) it.copy(memoSaving = MemoSavingState.Idle) else it
            }
        }

        private fun clearMemoDraft() {
            memoDraft = null
            savedStateHandle.remove<String>(MEMO_DRAFT_KEY)
            _uiState.updateContent { it.copy(memoDraft = null) }
        }

        private suspend fun saveMemoInternal(target: String?): Boolean {
            val attempt = ++memoAttempt
            _uiState.updateContent { it.copy(memoSaving = MemoSavingState.Saving(target)) }
            return runCatching { updateMemoUseCase(isbn, target) }
                .map { result ->
                    when (result) {
                        MemoChangeResult.Changed -> {
                            _uiState.updateContent { state ->
                                val currentMemo = state.bookDetail.entry?.memo
                                if (currentMemo == target) {
                                    val currentNormalizedDraft = state.memoDraft?.trim()?.ifEmpty { null }
                                    val shouldClearDraft = currentNormalizedDraft == target
                                    if (shouldClearDraft) {
                                        memoDraft = null
                                        savedStateHandle.remove<String>(MEMO_DRAFT_KEY)
                                    }
                                    state.copy(
                                        memoDraft = if (shouldClearDraft) null else state.memoDraft,
                                        memoSaving = MemoSavingState.Idle,
                                    )
                                } else {
                                    state
                                }
                            }
                            true
                        }

                        MemoChangeResult.Unchanged -> {
                            clearMemoDraft()
                            _uiState.updateContent { it.copy(memoSaving = MemoSavingState.Idle) }
                            true
                        }

                        MemoChangeResult.BookNotFound -> {
                            _uiState.updateContent { it.copy(memoSaving = MemoSavingState.Failed(target, attempt)) }
                            false
                        }
                    }
                }.getOrElse { e ->
                    if (e is CancellationException) {
                        _uiState.updateContent { it.copy(memoSaving = MemoSavingState.Idle) }
                        throw e
                    }
                    _uiState.updateContent { it.copy(memoSaving = MemoSavingState.Failed(target, attempt)) }
                    false
                }
        }

        fun addRecord(
            date: LocalDate,
            time: LocalTime,
            startPage: Int,
            endPage: Int,
        ): Long? {
            val content = _uiState.value as? BookDetailUiState.Content ?: return null
            if (content.recordSaving is RecordSavingState.Saving) return null
            val attempt = ++recordAttempt
            _uiState.updateContent { it.copy(recordSaving = RecordSavingState.Saving(attempt)) }
            viewModelScope.launch {
                runCatching {
                    addReadingRecord(
                        isbn = isbn,
                        date = date,
                        time = time,
                        startPage = startPage,
                        endPage = endPage,
                    )
                }.onSuccess { result ->
                    _uiState.updateContent { state ->
                        val check =
                            if (result.shouldCheckFinish) {
                                FinishCheckState.Pending(attempt, result.maxEndPage, state.bookDetail.book.totalPages ?: 0)
                            } else {
                                FinishCheckState.Idle
                            }
                        state.copy(recordSaving = RecordSavingState.Succeeded(attempt), finishCheck = check)
                    }
                }.onFailure { e ->
                    if (e is CancellationException) {
                        _uiState.updateContent { it.copy(recordSaving = RecordSavingState.Idle) }
                        throw e
                    }
                    _uiState.updateContent { it.copy(recordSaving = RecordSavingState.Failed(attempt)) }
                }
            }
            return attempt
        }

        fun dismissRecordError() {
            _uiState.updateContent {
                if (it.recordSaving is RecordSavingState.Failed) it.copy(recordSaving = RecordSavingState.Idle) else it
            }
        }

        fun confirmFinish(attempt: Long) {
            val content = _uiState.value as? BookDetailUiState.Content ?: return
            val check = content.finishCheck as? FinishCheckState.Active ?: return
            if (check is FinishCheckState.Confirming || check.attempt != attempt) return
            val confirming = FinishCheckState.Confirming(check.attempt, check.maxEndPage, check.totalPages)
            _uiState.updateContent { it.copy(finishCheck = confirming) }
            viewModelScope.launch {
                runCatching { changeStatus(isbn, ReadingStatus.FINISHED) }
                    .onSuccess { result ->
                        _uiState.updateContent { state ->
                            when (result) {
                                ReadingStatusChangeResult.Changed, ReadingStatusChangeResult.Unchanged -> {
                                    state.copy(finishCheck = FinishCheckState.Idle)
                                }

                                ReadingStatusChangeResult.BookNotFound, ReadingStatusChangeResult.InvalidStatus -> {
                                    state.copy(
                                        finishCheck = FinishCheckState.Failed(
                                            confirming.attempt,
                                            confirming.maxEndPage,
                                            confirming.totalPages,
                                        ),
                                    )
                                }
                            }
                        }
                    }
                    .onFailure { e ->
                        if (e is CancellationException) {
                            _uiState.updateContent { it.copy(finishCheck = FinishCheckState.Idle) }
                            throw e
                        }
                        _uiState.updateContent {
                            it.copy(
                                finishCheck = FinishCheckState.Failed(confirming.attempt, confirming.maxEndPage, confirming.totalPages),
                            )
                        }
                    }
            }
        }

        fun dismissFinishCheck() {
            _uiState.updateContent {
                if (it.finishCheck !is FinishCheckState.Idle) it.copy(finishCheck = FinishCheckState.Idle) else it
            }
        }

        private fun observeBookDetail() {
            if (observationJob?.isActive == true) return

            observationJob =
                viewModelScope.launch {
                    combine(
                        getBookDetail(isbn),
                        observeBookRecords.observeWithRetry(isbn, recordRetrySignals),
                    ) { bookDetail, recordObservation -> bookDetail to recordObservation }
                        .catch { _uiState.value = BookDetailUiState.Error }
                        .collect { (bookDetail, recordObservation) ->
                            _uiState.update { state ->
                                if (bookDetail != null) {
                                    val previous = state as? BookDetailUiState.Content
                                    val (records, recordLoadState) =
                                        resolveRecordObservation(previous?.records.orEmpty(), recordObservation)
                                    val tab =
                                        selectedTab ?: if (bookDetail.entry != null) {
                                            BookDetailTab.MyRecords
                                        } else {
                                            BookDetailTab.Information
                                        }
                                    selectedTab = tab
                                    val ratingSaving =
                                        resolveRatingSaving(previous?.ratingSaving, bookDetail.entry?.rating ?: 0)
                                    val memoSaving =
                                        resolveMemoSaving(previous?.memoSaving, bookDetail.entry?.memo)
                                    val currentDraft = resolveMemoDraft(previous, memoSaving)
                                    BookDetailUiState.Content(
                                        bookDetail = bookDetail,
                                        records = records,
                                        selectedTab = tab,
                                        refreshStatus = refreshStatus,
                                        statusChange = previous?.statusChange ?: StatusChangeState.Idle,
                                        recordSaving = previous?.recordSaving ?: RecordSavingState.Idle,
                                        recordLoadState = recordLoadState,
                                        finishCheck = previous?.finishCheck ?: FinishCheckState.Idle,
                                        ratingSaving = ratingSaving,
                                        memoDraft = currentDraft,
                                        memoSaving = memoSaving,
                                    )
                                } else {
                                    state
                                }
                            }
                        }
                }
        }

        private fun resolveMemoDraft(
            previous: BookDetailUiState.Content?,
            memoSaving: MemoSavingState,
        ): String? {
            if (previous?.memoSaving is MemoSavingState.Saving && memoSaving is MemoSavingState.Idle) {
                val savingTarget = previous.memoSaving.target
                val previousNormalizedDraft = previous.memoDraft?.trim()?.ifEmpty { null }
                if (previousNormalizedDraft == savingTarget) {
                    memoDraft = null
                    savedStateHandle.remove<String>(MEMO_DRAFT_KEY)
                    return null
                }
            }
            return previous?.memoDraft ?: memoDraft
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
            const val MEMO_DRAFT_KEY = "bookDetailMemoDraft"
        }
    }
