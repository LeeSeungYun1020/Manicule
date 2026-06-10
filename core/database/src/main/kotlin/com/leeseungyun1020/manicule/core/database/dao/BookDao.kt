package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(book: BookEntity)

    @Query("SELECT * FROM books WHERE isbn = :isbn")
    suspend fun getByIsbn(isbn: String): BookEntity?

    @Query("SELECT * FROM books WHERE isbn = :isbn")
    fun observeByIsbn(isbn: String): Flow<BookEntity?>
}
