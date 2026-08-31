package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.leeseungyun1020.manicule.core.database.dao.projection.DailyReadingProjection
import com.leeseungyun1020.manicule.core.database.dao.projection.ReadingTotalsProjection
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

@Dao
interface ReadingRecordStatsDao {
    @Query(
        """
        SELECT * FROM reading_records
        WHERE date >= :start AND date <= :end
        ORDER BY date ASC, time ASC, isbn ASC, id ASC
        """,
    )
    fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecordEntity>>

    @Query(
        """
        SELECT date,
            SUM(endPage - startPage + 1) AS pagesRead,
            COUNT(DISTINCT isbn) AS bookCount
        FROM reading_records
        WHERE date >= :start AND date <= :end
        GROUP BY date
        ORDER BY date ASC
        """,
    )
    fun observeDailyReading(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DailyReadingProjection>>

    @Query(
        """
        SELECT COALESCE(SUM(endPage - startPage + 1), 0) AS pagesRead,
            COUNT(DISTINCT isbn) AS bookCount
        FROM reading_records
        WHERE date >= :start AND date <= :end
        """,
    )
    fun observeTotals(
        start: LocalDate,
        end: LocalDate,
    ): Flow<ReadingTotalsProjection>

    @Query(
        """
        SELECT DISTINCT date FROM reading_records
        WHERE date <= :end
        ORDER BY date ASC
        """,
    )
    fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>>
}
