package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface StatsRepository {
    fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecord>>
}
