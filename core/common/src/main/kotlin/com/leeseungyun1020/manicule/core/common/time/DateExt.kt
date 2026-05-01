package com.leeseungyun1020.manicule.core.common.time

import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.until

/**
 * 한 주의 시작 요일. 잔디(ContributionGrid) 의 첫 행은 [WeekStart.dayOfWeek] 에 위치한다.
 *
 * Manicule 에서는 일요일을 한 주의 시작으로 본다 — 일반적인 그리드 레이아웃과 일치.
 */
object WeekStart {
    val dayOfWeek: DayOfWeek = DayOfWeek.SUNDAY
}

/**
 * 이 날짜가 속한 주의 시작일 (일요일) 을 반환.
 */
fun LocalDate.startOfWeek(weekStart: DayOfWeek = WeekStart.dayOfWeek): LocalDate {
    val current = this.dayOfWeek.isoDayNumber
    val target = weekStart.isoDayNumber
    val diff = ((current - target) + 7) % 7
    return this.minus(DatePeriod(days = diff))
}

/**
 * 이번 달의 1일.
 */
fun LocalDate.startOfMonth(): LocalDate = LocalDate(year, monthNumber, 1)

/**
 * 이번 달의 마지막 일자.
 */
fun LocalDate.endOfMonth(): LocalDate {
    val nextMonth = this.startOfMonth().plus(DatePeriod(months = 1))
    return nextMonth.minus(DatePeriod(days = 1))
}

/**
 * 올해의 1월 1일.
 */
fun LocalDate.startOfYear(): LocalDate = LocalDate(year, 1, 1)

/**
 * 올해의 12월 31일.
 */
fun LocalDate.endOfYear(): LocalDate = LocalDate(year, 12, 31)

/**
 * 두 날짜 사이의 일 수 (포함).
 */
fun LocalDate.daysUntilInclusive(other: LocalDate): Int =
    until(other, DateTimeUnit.DAY) + 1

/**
 * [start] 부터 [endInclusive] 까지의 모든 날짜를 순서대로 생성.
 */
fun dateRangeInclusive(start: LocalDate, endInclusive: LocalDate): Sequence<LocalDate> =
    generateSequence(start) { current ->
        if (current >= endInclusive) null else current.plus(DatePeriod(days = 1))
    }
