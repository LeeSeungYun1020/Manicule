package com.leeseungyun1020.manicule.core.domain.stats

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus

internal fun longestStreak(sortedDates: List<LocalDate>): Int {
    var longest = 0
    var current = 0
    var previous: LocalDate? = null

    sortedDates.forEach { date ->
        current =
            if (previous == null || date == previous.plus(DatePeriod(days = 1))) {
                current + 1
            } else {
                1
            }
        longest = maxOf(longest, current)
        previous = date
    }
    return longest
}

internal fun currentStreak(
    sortedDates: List<LocalDate>,
    today: LocalDate,
): Int {
    val lastDate = sortedDates.lastOrNull() ?: return 0
    val yesterday = today.minus(DatePeriod(days = 1))
    if (lastDate != today && lastDate != yesterday) return 0

    var streak = 1
    var expected = lastDate.minus(DatePeriod(days = 1))
    for (index in sortedDates.lastIndex - 1 downTo 0) {
        if (sortedDates[index] != expected) break
        streak += 1
        expected = expected.minus(DatePeriod(days = 1))
    }
    return streak
}
