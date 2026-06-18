package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ReadingRecordDao {
    @Upsert
    suspend fun upsertInternal(record: ReadingRecordEntity): Long

    @Query("SELECT id FROM reading_records WHERE isbn = :isbn AND date = :date")
    suspend fun getId(
        isbn: String,
        date: LocalDate,
    ): Long?

    @androidx.room.Transaction
    suspend fun upsert(record: ReadingRecordEntity): Long {
        val existingId = getId(record.isbn, record.date)
        return if (existingId != null) {
            upsertInternal(record.copy(id = existingId))
        } else {
            upsertInternal(record)
        }
    }

    @Query("DELETE FROM reading_records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM reading_records WHERE isbn = :isbn ORDER BY date DESC")
    fun observeByIsbn(isbn: String): Flow<List<ReadingRecordEntity>>

    @Query("SELECT * FROM reading_records WHERE date >= :start AND date <= :end ORDER BY date ASC")
    fun observeBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecordEntity>>

    @Query("SELECT cumulativePage FROM reading_records WHERE isbn = :isbn ORDER BY date DESC LIMIT 1")
    suspend fun latestCumulativeFor(isbn: String): Int?
}
