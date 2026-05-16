package com.leeseungyun1020.manicule.core.common.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 테스트 가능한 시간 출처. 코드에서 [kotlinx.datetime.Clock.System] 를 직접 호출하지 말고
 * 이 추상화를 사용한다.
 */
interface Clock {
    /** 현재 시각. */
    fun now(): Instant

    /** 현재 시각이 속한 타임존. */
    fun timeZone(): TimeZone

    /** 현재 로컬 날짜 — 잔디·통계 집계의 기준 키. */
    fun today(): LocalDate = now().toLocalDateTime(timeZone()).date
}

/**
 * 시스템 시계 / 시스템 기본 타임존을 사용하는 기본 구현.
 */
class SystemClock
    @javax.inject.Inject
    constructor() : Clock {
        override fun now(): Instant =
            kotlinx.datetime.Clock.System
                .now()

        override fun timeZone(): TimeZone = TimeZone.currentSystemDefault()
    }
