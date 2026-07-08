package com.leeseungyun1020.manicule.core.domain.record

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/** 기록 추가 결과 */
sealed interface AddRecordResult {
    data object Success : AddRecordResult

    /** 남은 페이지가 40쪽 이하 — 완독 여부 확인 필요 */
    data object NearlyFinished : AddRecordResult
}

class AddReadingRecordUseCase
    @Inject
    constructor(
        private val readingRecordRepository: ReadingRecordRepository,
        private val libraryRepository: LibraryRepository,
    ) {
        /**
         * 독서 기록 추가.
         * - 읽고싶음 상태 → 읽는 중 자동 전환
         * - 남은 페이지 40쪽 이하 시 NearlyFinished 반환
         */
        suspend operator fun invoke(
            isbn: String,
            date: LocalDate,
            cumulativePage: Int,
        ): AddRecordResult {
            TODO("3단계 Slice 2에서 구현")
        }
    }
