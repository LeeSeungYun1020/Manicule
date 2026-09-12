package com.leeseungyun1020.manicule.core.model

/**
 * 서재 목록 정렬 조건.
 *
 * @property criterion 정렬 기준
 * @property direction 정렬 방향
 */
data class LibrarySort(
    val criterion: Criterion,
    val direction: Direction,
) {
    enum class Criterion {
        ADDED_AT,
        UPDATED_AT,
        RATING,
    }

    enum class Direction {
        ASCENDING,
        DESCENDING,
    }

    companion object {
        val Default: LibrarySort =
            LibrarySort(
                criterion = Criterion.UPDATED_AT,
                direction = Direction.DESCENDING,
            )
    }
}
