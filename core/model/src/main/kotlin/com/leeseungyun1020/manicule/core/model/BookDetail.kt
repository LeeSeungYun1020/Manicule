package com.leeseungyun1020.manicule.core.model

/**
 * 책 상세 화면에 필요한 서지정보와 사용자의 등록 정보.
 *
 * [entry]가 null이면 아직 등록 정보나 리뷰가 없는 책이다.
 * 독서 상태를 설정하지 않고 리뷰만 저장한 경우에는 [entry]가 존재하고 상태는 [ReadingStatus.UNSET]이다.
 */
data class BookDetail(
    val book: Book,
    val entry: BookEntry?,
)
