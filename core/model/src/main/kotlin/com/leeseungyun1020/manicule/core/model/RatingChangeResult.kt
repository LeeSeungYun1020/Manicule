package com.leeseungyun1020.manicule.core.model

/** 별점 변경 결과. 저장소 오류는 예외로 전달한다. */
enum class RatingChangeResult {
    Changed,
    Unchanged,
    BookNotFound,
    InvalidRating,
}
