package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.TodaySummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTodaySummaryUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
        private val clock: Clock,
    ) {
        operator fun invoke(): Flow<TodaySummary> =
            clock.observeToday().flatMapLatest { today ->
                statsRepository.observeTotals(today, today).map { totals ->
                    TodaySummary(
                        date = today,
                        pagesRead = totals.pagesRead,
                        bookCount = totals.bookCount,
                    )
                }
            }
    }
