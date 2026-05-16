package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 잔디 한 칸. 하루 읽은 페이지 수를 0..4 단계로 양자화하여 색상 강도로 표현한다.
 *
 * 단계 기준 (plan.md 3.5):
 * - 0: 0 페이지
 * - 1: 1–19 페이지
 * - 2: 20–49 페이지
 * - 3: 50–99 페이지
 * - 4: 100+ 페이지
 */
data class ContributionDay(
    val date: LocalDate,
    val pages: Int,
    val intensity: Int,
) {
    init {
        require(intensity in 0..4) {
            "intensity must be in 0..4, was $intensity"
        }
    }

    companion object {
        fun intensityOf(pages: Int): Int =
            when {
                pages <= 0 -> 0
                pages < 20 -> 1
                pages < 50 -> 2
                pages < 100 -> 3
                else -> 4
            }

        fun of(
            date: LocalDate,
            pages: Int,
        ): ContributionDay = ContributionDay(date = date, pages = pages, intensity = intensityOf(pages))
    }
}
