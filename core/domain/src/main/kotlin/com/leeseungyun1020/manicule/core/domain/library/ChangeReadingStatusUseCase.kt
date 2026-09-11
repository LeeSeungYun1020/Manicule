package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class ChangeReadingStatusUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val clock: Clock,
    ) {
        /**
         * 독서 상태 변경. 완독 시 finishedAt 저장 규칙 포함.
         */
        suspend operator fun invoke(
            isbn: String,
            status: ReadingStatus,
        ): ReadingStatusChangeResult {
            if (status == ReadingStatus.UNSET) return ReadingStatusChangeResult.InvalidStatus
            val now = clock.now()
            return libraryRepository.changeReadingStatus(
                isbn = isbn,
                status = status,
                updatedAt = now,
                finishedAt = if (status == ReadingStatus.FINISHED) now.toLocalDateTime(clock.timeZone()).date else null,
            )
        }
    }
