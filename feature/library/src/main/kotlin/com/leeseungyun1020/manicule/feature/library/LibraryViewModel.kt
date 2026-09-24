package com.leeseungyun1020.manicule.feature.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.leeseungyun1020.manicule.core.domain.library.ChangeReadingStatusUseCase
import com.leeseungyun1020.manicule.core.domain.library.DeleteBookEntryUseCase
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.domain.library.RestoreBookEntryUseCase
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryRoute
import com.leeseungyun1020.manicule.feature.library.navigation.LibraryTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel
    @Inject
    constructor(
        getLibraryBooks: GetLibraryBooksUseCase,
        private val changeReadingStatus: ChangeReadingStatusUseCase,
        private val deleteBookEntry: DeleteBookEntryUseCase,
        private val restoreBookEntry: RestoreBookEntryUseCase,
        private val savedStateHandle: SavedStateHandle,
    ) : ViewModel() {
        private val initialStatus =
            LibraryTab.entries.firstOrNull { it.name == savedStateHandle.get<String>(SELECTED_TAB_KEY) }?.status
                ?: savedStateHandle.toRoute<LibraryRoute>().initialTab.status
        private val selectedStatus = MutableStateFlow(initialStatus)
        private val selectedSort = MutableStateFlow(LibrarySort.Default)
        private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        private val _actionMessage = MutableStateFlow<LibraryActionMessage?>(null)
        val actionMessage = _actionMessage
        private var nextActionId = 0L
        private var pendingUndo: PendingUndo? = null
        private val busyIsbns = mutableSetOf<String>()

        val uiState =
            combine(selectedStatus, selectedSort, retries.onStart { emit(Unit) }) { status, sort, _ -> status to sort }
                .flatMapLatest { (status, sort) ->
                    getLibraryBooks(status, sort)
                        .map<List<BookEntry>, LibraryUiState> { books ->
                            LibraryUiState.Content(status, books, sort)
                        }.onStart { emit(LibraryUiState.Loading(status, sort)) }
                        .catch { emit(LibraryUiState.Error(status, sort)) }
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

        fun selectSort(sort: LibrarySort) {
            selectedSort.value = sort
        }

        fun retry() {
            retries.tryEmit(Unit)
        }

        fun changeStatus(
            isbn: String,
            status: ReadingStatus,
        ) {
            val entry = currentEntry(isbn) ?: return
            if (status == ReadingStatus.UNSET || status == entry.status || !busyIsbns.add(isbn)) return
            val actionId = beginAction()
            viewModelScope.launch {
                try {
                    if (changeReadingStatus(isbn, status) == ReadingStatusChangeResult.Changed) {
                        completeAction(actionId, entry, LibraryActionMessageKind.STATUS_CHANGED)
                    } else {
                        showMessage(LibraryActionMessageKind.ACTION_FAILED, actionId)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    showMessage(LibraryActionMessageKind.ACTION_FAILED, actionId)
                } finally {
                    busyIsbns.remove(isbn)
                }
            }
        }

        fun deleteBook(isbn: String) {
            val entry = currentEntry(isbn) ?: return
            if (!busyIsbns.add(isbn)) return
            val actionId = beginAction()
            viewModelScope.launch {
                try {
                    deleteBookEntry(isbn)
                    completeAction(actionId, entry, LibraryActionMessageKind.DELETED)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    showMessage(LibraryActionMessageKind.ACTION_FAILED, actionId)
                } finally {
                    busyIsbns.remove(isbn)
                }
            }
        }

        fun undo(id: Long) {
            val pending = pendingUndo?.takeIf { it.id == id } ?: return
            if (!busyIsbns.add(pending.entry.book.isbn)) return
            viewModelScope.launch {
                try {
                    if (restoreBookEntry(pending.entry)) {
                        pendingUndo = null
                        if (_actionMessage.value?.id == id) _actionMessage.value = null
                    } else {
                        showMessage(LibraryActionMessageKind.UNDO_FAILED, id)
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    showMessage(LibraryActionMessageKind.UNDO_FAILED, id)
                } finally {
                    busyIsbns.remove(pending.entry.book.isbn)
                }
            }
        }

        fun messageDismissed(id: Long) {
            if (_actionMessage.value?.id == id) {
                _actionMessage.value = null
                if (pendingUndo?.id == id) pendingUndo = null
            }
        }

        private fun currentEntry(isbn: String): BookEntry? =
            (uiState.value as? LibraryUiState.Content)?.books?.firstOrNull { it.book.isbn == isbn }

        private fun beginAction(): Long {
            pendingUndo = null
            _actionMessage.value = null
            return ++nextActionId
        }

        private fun completeAction(
            id: Long,
            entry: BookEntry,
            kind: LibraryActionMessageKind,
        ) {
            if (id != nextActionId) return
            pendingUndo = PendingUndo(id, entry)
            _actionMessage.value = LibraryActionMessage(id, kind)
        }

        private fun showMessage(
            kind: LibraryActionMessageKind,
            id: Long = ++nextActionId,
        ) {
            if (id != nextActionId) return
            _actionMessage.value = LibraryActionMessage(id, kind)
        }

        private data class PendingUndo(
            val id: Long,
            val entry: BookEntry,
        )

        private companion object {
            const val SELECTED_TAB_KEY = "librarySelectedTab"
        }
    }
