package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReadingCalendarUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        /** 지난 365일 잔디 데이터 */
        operator fun invoke(): Flow<List<ReadingCalendarDay>> {
            TODO("3단계 Slice 4에서 구현")
        }
    }
