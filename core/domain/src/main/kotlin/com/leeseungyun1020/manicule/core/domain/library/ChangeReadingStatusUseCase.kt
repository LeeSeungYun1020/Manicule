package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import javax.inject.Inject

class ChangeReadingStatusUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        /**
         * 독서 상태 변경. 완독 시 finishedAt 저장 규칙 포함.
         */
        suspend operator fun invoke(
            isbn: String,
            status: ReadingStatus,
        ) {
            TODO("3단계 Slice 2에서 구현")
        }
    }
