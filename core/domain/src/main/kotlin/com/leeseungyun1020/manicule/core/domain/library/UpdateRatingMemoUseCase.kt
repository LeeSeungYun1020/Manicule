package com.leeseungyun1020.manicule.core.domain.library

import javax.inject.Inject

class UpdateRatingMemoUseCase
    @Inject
    constructor() {
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
