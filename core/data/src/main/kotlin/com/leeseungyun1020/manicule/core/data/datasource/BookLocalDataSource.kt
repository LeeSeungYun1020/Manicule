package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import kotlinx.coroutines.flow.Flow

interface BookLocalDataSource {
    suspend fun getByIsbn(isbn: String): BookEntity?

    fun observeByIsbn(isbn: String): Flow<BookEntity?>

    suspend fun upsert(book: BookEntity)
}
