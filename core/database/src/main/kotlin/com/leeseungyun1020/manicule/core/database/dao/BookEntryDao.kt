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
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Dao
interface BookEntryDao {
    /** 존재 확인, 최초 등록, 상태 변경을 하나의 트랜잭션으로 처리한다. */
    @Transaction
    suspend fun changeReadingStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Instant,
        finishedAt: LocalDate?,
    ): ReadingStatusChangeResult {
        if (status == ReadingStatus.UNSET) return ReadingStatusChangeResult.InvalidStatus
        require((status == ReadingStatus.FINISHED) == (finishedAt != null))
        if (!bookExists(isbn)) return ReadingStatusChangeResult.BookNotFound
        val entry = getEntry(isbn)
        if (entry?.status == status) return ReadingStatusChangeResult.Unchanged
        if (entry == null) {
            upsert(BookEntryEntity(isbn, status, 0, null, updatedAt, updatedAt, finishedAt))
        } else {
            updateStatus(isbn, status, updatedAt, finishedAt)
        }
        return ReadingStatusChangeResult.Changed
    }

    @Query("SELECT EXISTS(SELECT 1 FROM books WHERE isbn = :isbn)")
    suspend fun bookExists(isbn: String): Boolean

    @Query("SELECT * FROM book_entries WHERE isbn = :isbn")
    suspend fun getEntry(isbn: String): BookEntryEntity?

    @Query("UPDATE book_entries SET status = :status, updatedAt = :updatedAt, finishedAt = :finishedAt WHERE isbn = :isbn")
    suspend fun updateStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Instant,
        finishedAt: LocalDate?,
    )

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
            ORDER BY updatedAt DESC, isbn ASC
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
            ORDER BY updatedAt DESC, isbn ASC
        """,
    )
    fun observeAll(): Flow<List<BookEntryWithCurrentPage>>
}
