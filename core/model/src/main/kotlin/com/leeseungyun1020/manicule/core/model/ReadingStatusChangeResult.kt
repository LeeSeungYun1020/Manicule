package com.leeseungyun1020.manicule.core.model

/** 독서 상태 변경 결과. 저장소 오류는 예외로 전달한다. */
enum class ReadingStatusChangeResult {
    Changed,
    Unchanged,
    BookNotFound,
    InvalidStatus,
}
