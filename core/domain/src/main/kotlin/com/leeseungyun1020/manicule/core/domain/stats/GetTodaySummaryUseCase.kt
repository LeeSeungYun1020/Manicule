package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.TodaySummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodaySummaryUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(): Flow<TodaySummary> {
            TODO("3단계 Slice 4에서 구현")
        }
    }
