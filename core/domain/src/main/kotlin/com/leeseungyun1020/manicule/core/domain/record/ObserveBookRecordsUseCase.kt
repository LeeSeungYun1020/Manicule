package com.leeseungyun1020.manicule.core.domain.record

import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBookRecordsUseCase
    @Inject
    constructor(
        private val readingRecordRepository: ReadingRecordRepository,
    ) {
        operator fun invoke(isbn: String): Flow<List<ReadingRecord>> = readingRecordRepository.observeRecordsByIsbn(isbn)
    }
