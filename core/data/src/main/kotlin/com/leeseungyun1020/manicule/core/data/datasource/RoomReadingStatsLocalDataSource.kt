package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.ReadingRecordStatsDao
import com.leeseungyun1020.manicule.core.database.dao.projection.DailyReadingProjection
import com.leeseungyun1020.manicule.core.database.dao.projection.ReadingTotalsProjection
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class RoomReadingStatsLocalDataSource
    @Inject
    constructor(
        private val readingRecordStatsDao: ReadingRecordStatsDao,
    ) : ReadingStatsLocalDataSource {
        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecordEntity>> = readingRecordStatsDao.observeRecordsBetween(start, end)

        override fun observeDailyReading(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<DailyReadingProjection>> = readingRecordStatsDao.observeDailyReading(start, end)

        override fun observeTotals(
            start: LocalDate,
            end: LocalDate,
        ): Flow<ReadingTotalsProjection> = readingRecordStatsDao.observeTotals(start, end)

        override fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>> =
            readingRecordStatsDao.observeReadingDatesThrough(end)
    }
