package com.leeseungyun1020.manicule.core.domain.record

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import javax.inject.Inject

class DeleteReadingRecordUseCase
    @Inject
    constructor(
        private val readingRecordRepository: ReadingRecordRepository,
        private val clock: Clock,
    ) {
        suspend operator fun invoke(
            id: Long,
            isbn: String,
        ): Boolean {
            if (id <= 0 || isbn.isBlank()) return false
            return readingRecordRepository.removeRecord(id, isbn, clock.now())
        }
    }
