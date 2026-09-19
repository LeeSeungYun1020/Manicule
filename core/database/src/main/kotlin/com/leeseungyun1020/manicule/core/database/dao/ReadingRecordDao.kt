package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Dao
interface ReadingRecordDao {
    /** 세션 추가와 서재 등록·상태 전환을 원자적으로 처리하며, 실패 시 롤백한다. */
    @Transaction
    suspend fun add(
        record: ReadingRecordEntity,
        updatedAt: Instant,
    ): Long {
        require(record.id == 0L) { "A new record must have id 0" }
        require(record.isbn.isNotBlank()) { "isbn must not be blank" }
        require(record.startPage >= 1) { "startPage must be at least 1, was ${record.startPage}" }
        require(record.endPage >= record.startPage) { "endPage must be at least startPage, was ${record.endPage}" }
        registerEntryForNewRecord(record.isbn, updatedAt)
        updateEntryForNewRecord(record.isbn, updatedAt)
        return insert(record)
    }

    @Insert
    suspend fun insert(record: ReadingRecordEntity): Long

    @Query(
        """
        INSERT INTO book_entries (isbn, status, rating, memo, addedAt, updatedAt, finishedAt)
        SELECT :isbn, 'READING', 0, NULL, :updatedAt, :updatedAt, NULL
        WHERE NOT EXISTS(SELECT 1 FROM book_entries WHERE isbn = :isbn)
        """,
    )
    suspend fun registerEntryForNewRecord(
        isbn: String,
        updatedAt: Instant,
    )

    @Query(
        """
        UPDATE book_entries
        SET status = CASE
            WHEN status = 'UNSET' THEN 'READING'
            WHEN status = 'WANT' AND NOT EXISTS(SELECT 1 FROM reading_records WHERE isbn = :isbn)
            THEN 'READING' ELSE status END,
            updatedAt = :updatedAt
        WHERE isbn = :isbn
        """,
    )
    suspend fun updateEntryForNewRecord(
        isbn: String,
        updatedAt: Instant,
    )

    @Upsert
    suspend fun upsert(record: ReadingRecordEntity): Long

    @Query("DELETE FROM reading_records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM reading_records WHERE isbn = :isbn ORDER BY date DESC, time DESC")
    fun observeByIsbn(isbn: String): Flow<List<ReadingRecordEntity>>

    @Query("SELECT * FROM reading_records WHERE date >= :start AND date <= :end ORDER BY date DESC, time DESC")
    fun observeBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecordEntity>>

    @Query("SELECT MAX(endPage) FROM reading_records WHERE isbn = :isbn")
    suspend fun getMaxEndPage(isbn: String): Int?
}
