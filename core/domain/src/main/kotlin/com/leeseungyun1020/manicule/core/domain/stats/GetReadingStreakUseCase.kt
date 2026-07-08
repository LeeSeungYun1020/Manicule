package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.ReadingStreak
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReadingStreakUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
    ) {
        operator fun invoke(): Flow<ReadingStreak> {
            TODO("3단계 Slice 4에서 구현")
        }
    }
