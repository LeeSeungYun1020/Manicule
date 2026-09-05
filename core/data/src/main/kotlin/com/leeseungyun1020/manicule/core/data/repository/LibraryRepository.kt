package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
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

    fun observeAll(): Flow<List<BookEntry>>

    fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>>

    suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book>

    fun observeBookEntry(isbn: String): Flow<BookEntry?>

    suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult

    suspend fun removeBookEntry(isbn: String)
}
