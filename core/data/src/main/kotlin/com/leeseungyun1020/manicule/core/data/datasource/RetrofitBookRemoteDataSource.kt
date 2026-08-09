package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.network.nlk.NlkApi
import com.leeseungyun1020.manicule.core.network.nlk.NlkContentFetcher
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import javax.inject.Inject

class RetrofitBookRemoteDataSource
    @Inject
    constructor(
        private val nlkApi: NlkApi,
        private val contentFetcher: NlkContentFetcher,
    ) : BookRemoteDataSource {
        override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = nlkApi.searchBooks(pageNo = 1, pageSize = 1, isbn = isbn)

        override suspend fun searchBooksByTitle(
            query: String,
            page: Int,
            size: Int,
        ): NlkSearchResponseDto = nlkApi.searchBooks(pageNo = page, pageSize = size, title = query)

        override suspend fun searchBooksByAuthor(
            query: String,
            page: Int,
            size: Int,
        ): NlkSearchResponseDto = nlkApi.searchBooks(pageNo = page, pageSize = size, author = query)

        override suspend fun fetchNlkContent(url: String): String? = contentFetcher.fetch(url)
    }
