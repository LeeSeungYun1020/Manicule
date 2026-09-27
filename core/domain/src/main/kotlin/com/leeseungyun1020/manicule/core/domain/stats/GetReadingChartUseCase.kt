package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.common.di.Dispatcher
import com.leeseungyun1020.manicule.core.common.di.ManiculeDispatcher
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import javax.inject.Inject

enum class ReadingChartUnit { DAY, WEEK, MONTH }

data class ReadingChartBucket(
    val start: LocalDate,
    val end: LocalDate,
    val bookCount: Int,
    val pagesRead: Long,
)

class GetReadingChartUseCase
    @Inject
    constructor(
        private val repository: StatsRepository,
        @Dispatcher(ManiculeDispatcher.Default) private val dispatcher: CoroutineDispatcher,
    ) {
        operator fun invoke(
            start: LocalDate,
            end: LocalDate,
            unit: ReadingChartUnit,
        ): Flow<List<ReadingChartBucket>> {
            require(start <= end) { "start must be on or before end" }
            return repository.observeRecordsBetween(start, end).map { records -> aggregate(start, end, unit, records) }
                .flowOn(dispatcher)
        }
    }

internal fun aggregate(
    start: LocalDate,
    end: LocalDate,
    unit: ReadingChartUnit,
    records: List<ReadingRecord>,
): List<ReadingChartBucket> {
    val ranges = buildList {
        var cursor = start
        while (cursor <= end) {
            val boundary = when (unit) {
                ReadingChartUnit.DAY -> cursor
                ReadingChartUnit.WEEK -> cursor.plus(DatePeriod(days = 6 - cursor.dayOfWeek.ordinal))
                ReadingChartUnit.MONTH -> LocalDate(cursor.year, cursor.monthNumber, 1)
                    .plus(DatePeriod(months = 1)).minus(DatePeriod(days = 1))
            }
            val last = minOf(boundary, end)
            add(cursor to last)
            cursor = last.plus(DatePeriod(days = 1))
        }
    }
    val counts = Array(ranges.size) { mutableSetOf<String>() }
    val pages = LongArray(ranges.size)
    val indices = HashMap<LocalDate, Int>()
    ranges.forEachIndexed { index, (first, last) ->
        var date = first
        while (date <= last) {
            indices[date] = index
            date = date.plus(DatePeriod(days = 1))
        }
    }
    records.forEach { record ->
        indices[record.date]?.let { index ->
            counts[index].add(record.isbn)
            pages[index] += record.pagesRead.toLong()
        }
    }
    return ranges.mapIndexed { index, (first, last) ->
        ReadingChartBucket(first, last, counts[index].size, pages[index])
    }
}
