package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.network.nlk.NlkApi
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import javax.inject.Inject

class BookRemoteDataSourceImpl
    @Inject
    constructor(
        private val nlkApi: NlkApi,
    ) : BookRemoteDataSource {
        override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = nlkApi.searchBooks(pageNo = 1, pageSize = 1, isbn = isbn)
    }
