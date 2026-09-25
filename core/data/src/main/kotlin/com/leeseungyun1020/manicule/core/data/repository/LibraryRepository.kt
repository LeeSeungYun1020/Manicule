package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

sealed interface SaveBookEntryResult {
    data object Saved : SaveBookEntryResult

    data class InvalidRating(
        val rating: Int,
    ) : SaveBookEntryResult
}

interface LibraryRepository {
    /** 상태와 시각만 원자적으로 변경한다. 최초 등록은 캐시된 책이 있어야 한다.
     * 같은 상태는 시각을 보존하며, FINISHED일 때만 finishedAt을 전달한다.
     */
    suspend fun changeReadingStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Instant,
        finishedAt: LocalDate?,
    ): ReadingStatusChangeResult

    /** 별점과 시각만 원자적으로 변경한다. 최초 등록은 UNSET 상태로 캐시된 책이 있어야 한다.
     * 같은 별점은 시각을 보존하며, 항목이 없고 0점이면 변경하지 않는다.
     */
    suspend fun updateRating(
        isbn: String,
        rating: Int,
        updatedAt: Instant,
    ): RatingChangeResult

    fun observeAll(): Flow<List<BookEntry>>

    fun observeByStatus(
        status: ReadingStatus,
        sort: LibrarySort = LibrarySort.Default,
    ): Flow<List<BookEntry>>

    suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book>

    fun observeBookEntry(isbn: String): Flow<BookEntry?>

    suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult

    suspend fun restoreDeletedEntryIfAbsent(entry: BookEntry): Boolean

    suspend fun restoreReadingStatusIfUnchanged(
        original: BookEntry,
        changedStatus: ReadingStatus,
        changedAt: Instant,
    ): Boolean

    suspend fun removeBookEntry(isbn: String)
}
