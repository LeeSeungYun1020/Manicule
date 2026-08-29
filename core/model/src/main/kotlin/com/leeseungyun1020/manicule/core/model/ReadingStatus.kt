package com.leeseungyun1020.manicule.core.model

/**
 * 사용자의 책에 대한 독서 상태.
 *
 * - [UNSET]   : 독서 상태를 선택하지 않음
 * - [WANT]    : 읽고 싶은 책 (위시리스트)
 * - [READING] : 현재 읽고 있는 책
 * - [FINISHED]: 완독한 책
 */
enum class ReadingStatus {
    UNSET,
    WANT,
    READING,
    FINISHED,
}
