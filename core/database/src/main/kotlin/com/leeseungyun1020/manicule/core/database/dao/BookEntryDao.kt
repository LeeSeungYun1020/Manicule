package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BookEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BookEntryEntity)

    @Query("DELETE FROM book_entries WHERE isbn = :isbn")
    suspend fun delete(isbn: String)

    @Query(
        value = """
            SELECT *, 
            (SELECT cumulativePage FROM reading_records WHERE isbn = book_entries.isbn ORDER BY date DESC LIMIT 1) AS currentPage 
            FROM book_entries 
            WHERE isbn = :isbn
        """,
    )
    fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?>

    @Query(
        value = """
            SELECT *, 
            (SELECT cumulativePage FROM reading_records WHERE isbn = book_entries.isbn ORDER BY date DESC LIMIT 1) AS currentPage 
            FROM book_entries 
            WHERE status = :status
        """,
    )
    fun observeByStatus(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    @Query(
        value = """
            SELECT *, 
            (SELECT cumulativePage FROM reading_records WHERE isbn = book_entries.isbn ORDER BY date DESC LIMIT 1) AS currentPage 
            FROM book_entries
        """,
    )
    fun observeAll(): Flow<List<BookEntryWithCurrentPage>>
}
