package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * 사용자의 서재에 등록된 책 한 권의 상태 정보.
 *
 * @property book          기본 서지정보
 * @property status        독서 상태(WANT/READING/FINISHED)
 * @property rating        별점 (1..5, 미입력 시 null)
 * @property memo          한 줄 메모
 * @property addedAt       서재에 처음 추가된 시각
 * @property updatedAt     마지막 수정 시각 (정렬에 사용)
 * @property finishedAt    완독 일자 (FINISHED 일 때만 의미 있음)
 * @property currentPage   가장 최근 [ReadingRecord.cumulativePage]. 진도 표시용 캐시 — 미기록 시 null.
 */
data class BookEntry(
    val book: Book,
    val status: ReadingStatus,
    val rating: Int? = null,
    val memo: String? = null,
    val addedAt: Instant,
    val updatedAt: Instant,
    val finishedAt: LocalDate? = null,
    val currentPage: Int? = null,
) {
    init {
        require(rating == null || rating in 1..5) {
            "rating must be in 1..5, was $rating"
        }
    }
}
