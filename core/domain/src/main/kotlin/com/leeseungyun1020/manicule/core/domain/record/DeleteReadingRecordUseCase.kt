package com.leeseungyun1020.manicule.core.domain.record

import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import javax.inject.Inject

class DeleteReadingRecordUseCase
    @Inject
    constructor(
        private val readingRecordRepository: ReadingRecordRepository,
    ) {
        suspend operator fun invoke(id: Long) {
            readingRecordRepository.removeRecord(id)
        }
    }
