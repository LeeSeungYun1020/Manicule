package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class GetPeriodSummaryUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(
            start: LocalDate,
            end: LocalDate,
        ): Flow<PeriodSummary> {
            require(start <= end) {
                "start must be on or before end, was start=$start end=$end"
            }
            return combine(
                statsRepository.observeDailyReading(start, end),
                statsRepository.observeTotals(start, end),
            ) { readings, totals ->
                PeriodSummary(
                    rangeStart = start,
                    rangeEnd = end,
                    longestStreak = longestStreak(readings.map { it.date }),
                    pagesRead = totals.pagesRead,
                    bookCount = totals.bookCount,
                )
            }
        }
    }
