package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.ReadingStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetReadingStreakUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
        private val clock: Clock,
    ) {
        operator fun invoke(): Flow<ReadingStreak> {
            val today = clock.today()
            return statsRepository.observeReadingDatesThrough(today).map { dates ->
                val validDates = dates.filter { it <= today }.distinct().sorted()
                if (validDates.isEmpty()) {
                    ReadingStreak.Empty
                } else {
                    ReadingStreak(
                        current = currentStreak(validDates, today),
                        longest = longestStreak(validDates),
                        lastDate = validDates.last(),
                    )
                }
            }
        }
    }
