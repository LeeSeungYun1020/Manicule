package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import javax.inject.Inject

class UpdateRatingMemoUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        /**
         * 별점·메모 수정.
         */
        suspend operator fun invoke(
            isbn: String,
            rating: Int?,
            memo: String?,
        ) {
            TODO("3단계 Slice 2에서 구현")
        }
    }
