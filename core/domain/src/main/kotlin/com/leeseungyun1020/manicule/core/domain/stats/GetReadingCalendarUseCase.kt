package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.common.time.dateRangeInclusive
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetReadingCalendarUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingCalendarDay>> {
            require(start <= end) {
                "start must be on or before end, was start=$start end=$end"
            }
            return statsRepository.observeDailyReading(start, end).map { readings ->
                val pagesByDate = readings.associate { it.date to it.pagesRead }
                dateRangeInclusive(start, end).map { date ->
                    ReadingCalendarDay.of(date, pagesByDate[date] ?: 0)
                }.toList()
            }
        }
    }
