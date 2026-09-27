package com.leeseungyun1020.manicule.core.domain.record

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import javax.inject.Inject

class EditReadingRecordUseCase
    @Inject
    constructor(
        private val readingRecordRepository: ReadingRecordRepository,
        private val clock: Clock,
    ) {
        suspend operator fun invoke(record: ReadingRecord): Boolean {
            if (record.id <= 0) return false
            return readingRecordRepository.saveRecord(record, clock.now())
        }
    }
