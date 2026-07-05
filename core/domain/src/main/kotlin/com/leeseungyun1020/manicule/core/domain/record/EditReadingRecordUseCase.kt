package com.leeseungyun1020.manicule.core.domain.record

import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import javax.inject.Inject

class EditReadingRecordUseCase
    @Inject
    constructor(
        private val readingRecordRepository: ReadingRecordRepository,
    ) {
        suspend operator fun invoke(record: ReadingRecord) {
            TODO("3단계 Slice 2에서 구현")
        }
    }
