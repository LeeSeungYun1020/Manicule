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
