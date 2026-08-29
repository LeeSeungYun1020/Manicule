package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Upsert
    suspend fun upsert(book: BookEntity)

    @Query("SELECT * FROM books WHERE isbn = :isbn")
    suspend fun getByIsbn(isbn: String): BookEntity?

    @Query("SELECT * FROM books WHERE isbn = :isbn")
    fun observeByIsbn(isbn: String): Flow<BookEntity?>
}
