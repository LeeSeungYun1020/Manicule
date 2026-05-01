package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 통계 화면의 기간 단위.
 */
enum class StatsPeriod {
    THIS_MONTH,
    THIS_YEAR,
}

/**
 * 통계 요약 정보.
 *
 * @property period             요약이 가리키는 기간
 * @property rangeStart         포함 시작 일자
 * @property rangeEnd           포함 종료 일자 (포함)
 * @property finishedBookCount  해당 기간에 완독한 책의 권 수
 * @property pagesRead          해당 기간에 읽은 총 페이지 수
 */
data class PeriodSummary(
    val period: StatsPeriod,
    val rangeStart: LocalDate,
    val rangeEnd: LocalDate,
    val finishedBookCount: Int,
    val pagesRead: Int,
)
