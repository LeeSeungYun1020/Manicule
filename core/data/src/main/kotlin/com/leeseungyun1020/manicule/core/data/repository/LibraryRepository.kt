package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {
    fun observeAll(): Flow<List<BookEntry>>

    fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>>

    suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<Book>

    fun observeBookEntry(isbn: String): Flow<BookEntry?>

    suspend fun saveBookEntry(entry: BookEntry)

    suspend fun removeBookEntry(isbn: String)
}
