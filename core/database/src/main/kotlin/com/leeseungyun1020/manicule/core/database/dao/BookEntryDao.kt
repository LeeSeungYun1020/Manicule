package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BookEntryEntity)

    @Query("DELETE FROM book_entries WHERE isbn = :isbn")
    suspend fun delete(isbn: String)

    @Transaction
    @Query(
        value = """
            SELECT *, 
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries 
            WHERE isbn = :isbn
        """,
    )
    fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?>

    @Transaction
    @Query(
        value = """
            SELECT *, 
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries 
            WHERE status = :status
        """,
    )
    fun observeByStatus(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    @Query(
        value = """
            SELECT books.*
            FROM book_entries
            INNER JOIN books ON books.isbn = book_entries.isbn
            WHERE book_entries.status = :status
            ORDER BY book_entries.updatedAt DESC, book_entries.isbn ASC
            LIMIT :limit
        """,
    )
    suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<BookEntity>

    @Transaction
    @Query(
        value = """
            SELECT *, 
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries
        """,
    )
    fun observeAll(): Flow<List<BookEntryWithCurrentPage>>
}
