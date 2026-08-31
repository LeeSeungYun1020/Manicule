package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.projection.DailyReadingProjection
import com.leeseungyun1020.manicule.core.database.dao.projection.ReadingTotalsProjection
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ReadingStatsLocalDataSource {
    fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecordEntity>>

    fun observeDailyReading(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DailyReadingProjection>>

    fun observeTotals(
        start: LocalDate,
        end: LocalDate,
    ): Flow<ReadingTotalsProjection>

    fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>>
}
