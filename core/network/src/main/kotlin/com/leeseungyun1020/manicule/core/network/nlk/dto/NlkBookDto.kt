package com.leeseungyun1020.manicule.core.network.nlk.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국립중앙도서관 ISBN 서지정보 API의 개별 도서 DTO.
 *
 * API 응답 필드 중 앱에서 활용하는 항목만 매핑한다.
 * 사용하지 않는 필드(VOL, SERIES_TITLE 등)는 생략한다.
 */
@Serializable
data class NlkBookDto(
    @SerialName("EA_ISBN") val isbn: String = "",
    @SerialName("TITLE") val title: String = "",
    @SerialName("AUTHOR") val author: String = "",
    @SerialName("PUBLISHER") val publisher: String = "",
    /** yyyyMMdd 형식의 발행예정일. 빈 문자열일 수 있다. */
    @SerialName("PUBLISH_PREDATE") val publishPredate: String = "",
    /** 표지 이미지 URL. 빈 문자열일 수 있다. */
    @SerialName("TITLE_URL") val titleUrl: String = "",
    /** 예정가격 (원). 빈 문자열이면 가격 정보 없음. */
    @SerialName("PRE_PRICE") val prePrice: String = "",
    /** 한국십진분류 주제어. */
    @SerialName("SUBJECT") val subject: String = "",
    /** 목차 URL. 빈 문자열이면 목차 없음. */
    @SerialName("BOOK_TB_CNT_URL") val bookTbCntUrl: String = "",
    /** 책 소개 URL. 빈 문자열이면 소개 없음. */
    @SerialName("BOOK_INTRODUCTION_URL") val bookIntroductionUrl: String = "",
    /** 요약 URL. 빈 문자열이면 요약 없음. */
    @SerialName("BOOK_SUMMARY_URL") val bookSummaryUrl: String = "",
    /** 페이지 수. 빈 문자열이면 정보 없음. */
    @SerialName("PAGE") val page: String = "",
)
