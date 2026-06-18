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
    suspend fun upsert(record: ReadingRecordEntity): Long

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
