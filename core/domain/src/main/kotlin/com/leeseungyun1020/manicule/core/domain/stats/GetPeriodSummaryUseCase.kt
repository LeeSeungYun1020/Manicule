package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.PeriodSummary
import com.leeseungyun1020.manicule.core.model.StatsPeriod
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPeriodSummaryUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(period: StatsPeriod): Flow<PeriodSummary> {
            TODO("3단계 Slice 4에서 구현")
        }
    }
