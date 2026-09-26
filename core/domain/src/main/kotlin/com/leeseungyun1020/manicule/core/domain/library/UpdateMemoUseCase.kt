package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.MemoChangeResult
import javax.inject.Inject

class UpdateMemoUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val clock: Clock,
    ) {
        /**
         * 메모 수정. 앞뒤 공백을 제거하고 빈 문자열만 남으면 null로 정규화한다.
         */
        suspend operator fun invoke(
            isbn: String,
            memo: String?,
        ): MemoChangeResult {
            val normalizedMemo = memo?.trim()?.ifEmpty { null }
            return libraryRepository.updateMemo(
                isbn = isbn,
                memo = normalizedMemo,
                updatedAt = clock.now(),
            )
        }
    }
