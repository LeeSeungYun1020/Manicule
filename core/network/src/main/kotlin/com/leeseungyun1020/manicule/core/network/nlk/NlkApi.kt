package com.leeseungyun1020.manicule.core.network.nlk

import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** 국립중앙도서관 ISBN 서지정보 API Retrofit 인터페이스. */
interface NlkApi {

    @GET("seoji/SearchApi.do")
    suspend fun searchBooks(
        @Query("cert_key") certKey: String,
        @Query("result_style") resultStyle: String = "json",
        @Query("page_no") pageNo: Int,
        @Query("page_size") pageSize: Int,
        @Query("title") title: String? = null,
        @Query("author") author: String? = null,
        @Query("isbn") isbn: String? = null,
    ): NlkSearchResponseDto
}
