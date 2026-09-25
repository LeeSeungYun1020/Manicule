package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import javax.inject.Inject

class UpdateRatingUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
        private val clock: Clock,
    ) {
        /**
         * 별점 수정. 허용 값은 0..5이며 범위를 벗어나면 InvalidRating을 반환한다.
         */
        suspend operator fun invoke(
            isbn: String,
            rating: Int,
        ): RatingChangeResult {
            if (rating !in MIN_RATING..MAX_RATING) return RatingChangeResult.InvalidRating
            return libraryRepository.updateRating(
                isbn = isbn,
                rating = rating,
                updatedAt = clock.now(),
            )
        }

        companion object {
            const val MIN_RATING = 0
            const val MAX_RATING = 5
        }
    }
