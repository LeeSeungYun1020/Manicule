package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    fun observeBook(isbn: String): Flow<Book?>

    suspend fun syncBook(isbn: String): Result<Unit>
}
