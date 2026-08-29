package com.leeseungyun1020.manicule.core.data.repository

import androidx.paging.PagingData
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun observeBook(isbn: String): Flow<Book?>

    suspend fun syncBook(isbn: String): Result<BookSyncStatus>

    fun searchBooks(query: String): Flow<PagingData<Book>>
}
