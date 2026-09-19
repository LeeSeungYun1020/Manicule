package com.leeseungyun1020.manicule.core.domain.record

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject

class AddReadingRecordUseCase
    @Inject
    constructor(
        private val readingRecordRepository: ReadingRecordRepository,
        private val clock: Clock,
    ) {
        /** 새 세션 ID를 반환한다. 미등록 책은 READING으로 등록하고 UNSET·첫 WANT 기록은 READING으로 전환한다. */
        suspend operator fun invoke(
            isbn: String,
            date: LocalDate,
            time: LocalTime,
            startPage: Int,
            endPage: Int,
        ): Long {
            val record = ReadingRecord(0, isbn, date, time, startPage, endPage)
            return readingRecordRepository.addRecord(record, clock.now())
        }
    }
