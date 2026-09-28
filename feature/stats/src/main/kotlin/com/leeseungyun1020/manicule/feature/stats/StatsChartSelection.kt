package com.leeseungyun1020.manicule.feature.stats

import com.leeseungyun1020.manicule.core.domain.stats.ReadingChartUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil

internal fun defaultChartUnit(
    period: StatsPeriod,
    start: LocalDate,
    end: LocalDate,
): ReadingChartUnit =
    when (period) {
        StatsPeriod.TODAY -> ReadingChartUnit.DAY
        StatsPeriod.FOUR_WEEKS -> ReadingChartUnit.WEEK
        StatsPeriod.ONE_YEAR -> ReadingChartUnit.MONTH
        StatsPeriod.CUSTOM -> when (start.daysUntil(end) + 1) {
            in 1..14 -> ReadingChartUnit.DAY
            in 15..90 -> ReadingChartUnit.WEEK
            else -> ReadingChartUnit.MONTH
        }
    }

internal fun chartUnitFromSaved(
    raw: String?,
    fallback: ReadingChartUnit,
): ReadingChartUnit = ReadingChartUnit.entries.firstOrNull { it.name == raw } ?: fallback
