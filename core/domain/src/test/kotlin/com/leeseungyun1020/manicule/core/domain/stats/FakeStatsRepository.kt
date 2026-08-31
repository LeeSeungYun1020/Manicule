package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.DailyReading
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingTotals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone

internal class FakeStatsRepository : StatsRepository {
    val records = MutableStateFlow<List<ReadingRecord>>(emptyList())
    val daily = MutableStateFlow<List<DailyReading>>(emptyList())
    val totals = MutableStateFlow(ReadingTotals(0, 0))
    val dates = MutableStateFlow<List<LocalDate>>(emptyList())
    var lastRange: Pair<LocalDate, LocalDate>? = null
    var datesEnd: LocalDate? = null

    override fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecord>> {
        lastRange = start to end
        return records
    }

    override fun observeDailyReading(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DailyReading>> {
        lastRange = start to end
        return daily
    }

    override fun observeTotals(
        start: LocalDate,
        end: LocalDate,
    ): Flow<ReadingTotals> {
        lastRange = start to end
        return totals
    }

    override fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>> {
        datesEnd = end
        return dates
    }
}

internal class FixedClock(
    private val instant: Instant,
    private val zone: TimeZone,
) : Clock {
    override fun now(): Instant = instant

    override fun timeZone(): TimeZone = zone
}

internal class MutableClock(
    var instant: Instant,
    private val zone: TimeZone,
) : Clock {
    override fun now(): Instant = instant

    override fun timeZone(): TimeZone = zone
}
