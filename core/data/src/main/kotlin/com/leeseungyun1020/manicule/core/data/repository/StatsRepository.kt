package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.DailyReading
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingTotals
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface StatsRepository {
    fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecord>>

    fun observeDailyReading(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DailyReading>>

    fun observeTotals(
        start: LocalDate,
        end: LocalDate,
    ): Flow<ReadingTotals>

    fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>>
}
