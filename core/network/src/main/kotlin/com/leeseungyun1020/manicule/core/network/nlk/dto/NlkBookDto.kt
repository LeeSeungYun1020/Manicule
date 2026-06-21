package com.leeseungyun1020.manicule.core.network.nlk.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 국립중앙도서관 ISBN 서지정보 API의 개별 도서 DTO.
 */
@Serializable
data class NlkBookDto(
    @SerialName("PUBLISHER") val publisher: String = "",
    @SerialName("DDC") val ddc: String = "",
    @SerialName("UPDATE_DATE") val updateDate: String = "",
    @SerialName("BOOK_TB_CNT") val bookTbCnt: String = "",
    @SerialName("BOOK_SUMMARY") val bookSummary: String = "",
    @SerialName("EA_ADD_CODE") val eaAddCode: String = "",
    @SerialName("PUBLISHER_URL") val publisherUrl: String = "",
    @SerialName("AUTHOR") val author: String = "",
    @SerialName("SERIES_TITLE") val seriesTitle: String = "",
    @SerialName("KDC") val kdc: String = "",
    @SerialName("EDITION_STMT") val editionStmt: String = "",
    @SerialName("BOOK_TB_CNT_URL") val bookTbCntUrl: String = "",
    @SerialName("SET_ISBN") val setIsbn: String = "",
    @SerialName("REAL_PUBLISH_DATE") val realPublishDate: String = "",
    @SerialName("TITLE_URL") val titleUrl: String = "",
    @SerialName("PRE_PRICE") val prePrice: String = "",
    @SerialName("BOOK_INTRODUCTION_URL") val bookIntroductionUrl: String = "",
    @SerialName("DEPOSIT_YN") val depositYn: String = "",
    @SerialName("BOOK_SIZE") val bookSize: String = "",
    @SerialName("BOOK_SUMMARY_URL") val bookSummaryUrl: String = "",
    @SerialName("EBOOK_YN") val ebookYn: String = "",
    @SerialName("REAL_PRICE") val realPrice: String = "",
    @SerialName("FORM") val form: String = "",
    @SerialName("FORM_DETAIL") val formDetail: String = "",
    @SerialName("PAGE") val page: String = "",
    @SerialName("CONTROL_NO") val controlNo: String = "",
    @SerialName("SERIES_NO") val seriesNo: String = "",
    @SerialName("EA_ISBN") val isbn: String = "",
    @SerialName("INPUT_DATE") val inputDate: String = "",
    @SerialName("BOOK_INTRODUCTION") val bookIntroduction: String = "",
    @SerialName("SET_EXPRESSION") val setExpression: String = "",
    @SerialName("VOL") val vol: String = "",
    @SerialName("CIP_YN") val cipYn: String = "",
    @SerialName("SUBJECT") val subject: String = "",
    @SerialName("BIB_YN") val bibYn: String = "",
    @SerialName("TITLE") val title: String = "",
    @SerialName("PUBLISH_PREDATE") val publishPredate: String = "",
    @SerialName("RELATED_ISBN") val relatedIsbn: String = "",
    @SerialName("SET_ADD_CODE") val setAddCode: String = "",
)
