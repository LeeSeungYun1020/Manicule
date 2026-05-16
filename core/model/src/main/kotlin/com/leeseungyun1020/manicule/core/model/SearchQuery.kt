package com.leeseungyun1020.manicule.core.model

import kotlinx.datetime.Instant

/**
 * 최근 검색어 한 건.
 *
 * @property query     검색에 사용한 문자열
 * @property executedAt 마지막으로 실행된 시각 — 정렬 키
 */
data class SearchQuery(
    val query: String,
    val executedAt: Instant,
)
