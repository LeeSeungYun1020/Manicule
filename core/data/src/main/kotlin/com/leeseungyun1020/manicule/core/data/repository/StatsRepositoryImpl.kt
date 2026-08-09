package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.datasource.ReadingStatsLocalDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.model.DailyReading
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingTotals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class StatsRepositoryImpl
    @Inject
    constructor(
        private val readingStatsLocalDataSource: ReadingStatsLocalDataSource,
    ) : StatsRepository {
        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> {
            requireValidRange(start, end)
            return readingStatsLocalDataSource
                .observeRecordsBetween(start, end)
                .map { list ->
                    list.map { it.asExternalModel() }
                }
        }

        override fun observeDailyReading(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<DailyReading>> {
            requireValidRange(start, end)
            return readingStatsLocalDataSource
                .observeDailyReading(start, end)
                .map { readings ->
                    readings.map { reading ->
                        DailyReading(
                            date = reading.date,
                            pagesRead = reading.pagesRead,
                            bookCount = reading.bookCount,
                        )
                    }
                }
        }

        override fun observeTotals(
            start: LocalDate,
            end: LocalDate,
        ): Flow<ReadingTotals> {
            requireValidRange(start, end)
            return readingStatsLocalDataSource
                .observeTotals(start, end)
                .map { totals ->
                    ReadingTotals(
                        pagesRead = totals.pagesRead,
                        bookCount = totals.bookCount,
                    )
                }
        }

        override fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>> =
            readingStatsLocalDataSource.observeReadingDatesThrough(end)

        private fun requireValidRange(
            start: LocalDate,
            end: LocalDate,
        ) {
            require(start <= end) {
                "start must be on or before end, was start=$start end=$end"
            }
        }
    }
