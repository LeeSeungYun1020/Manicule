package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
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
            return statsRepository.observeRecordsBetween(start, end).map { records ->
                PeriodSummary(
                    rangeStart = start,
                    rangeEnd = end,
                    longestStreak = longestStreak(records.map { it.date }),
                    pagesRead = records.sumOf { it.pagesRead },
                    bookCount = records.distinctBy { it.isbn }.size,
                )
            }
        }
    }
