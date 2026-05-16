package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.LocalDate

/**
 * 도메인에서 사용하는 책 한 권의 서지정보.
 *
 * 국립중앙도서관(NLK) ISBN 서지정보 API 응답을 도메인 모델로 매핑한다.
 * 매핑 출처는 괄호로 표기하였다.
 */
data class Book(
    /** EA_ISBN — 국제표준도서번호. 도메인의 식별자로 사용. */
    val isbn: String,
    /** TITLE */
    val title: String,
    /** AUTHOR */
    val author: String,
    /** PUBLISHER */
    val publisher: String,
    /** PUBLISH_PREDATE */
    val publishedDate: LocalDate?,
    /** TITLE_URL — 표지 이미지 URL */
    val coverUrl: String?,
    /** PAGE — 전체 페이지 수 */
    val totalPages: Int?,
    /** PRE_PRICE — 표시가격 (원) */
    val price: Int?,
    /** SUBJECT — 한국십진분류 분류명 */
    val category: String?,
    /** BOOK_TB_CNT_URL — 목차 텍스트가 담긴 URL */
    val tableOfContentsUrl: String?,
    /** BOOK_INTRODUCTION_URL — 책 소개가 담긴 URL */
    val introductionUrl: String?,
    /** BOOK_SUMMARY_URL — 요약이 담긴 URL */
    val summaryUrl: String?,
)
