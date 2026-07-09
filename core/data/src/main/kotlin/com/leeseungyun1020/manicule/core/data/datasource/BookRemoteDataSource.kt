package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto

interface BookRemoteDataSource {
    suspend fun searchBooks(isbn: String): NlkSearchResponseDto

    suspend fun searchBooksByTitle(
        query: String,
        page: Int,
        size: Int,
    ): NlkSearchResponseDto

    suspend fun searchBooksByAuthor(
        query: String,
        page: Int,
        size: Int,
    ): NlkSearchResponseDto
}
