package com.leeseungyun1020.manicule.core.network.nlk.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 국립중앙도서관 ISBN 서지정보 API 최상위 응답. */
@Serializable
data class NlkSearchResponseDto(
    @SerialName("TOTAL_COUNT") val totalCount: String = "0",
    @SerialName("PAGE_NO") val pageNo: String = "1",
    @SerialName("docs") val docs: List<NlkBookDto> = emptyList(),
)
