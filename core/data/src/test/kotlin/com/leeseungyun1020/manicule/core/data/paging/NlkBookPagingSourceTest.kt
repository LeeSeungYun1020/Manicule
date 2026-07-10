package com.leeseungyun1020.manicule.core.data.paging

import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSource
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkBookDto
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Test

class NlkBookPagingSourceTest {

    private val dummyTitleBooks =
        listOf(
            NlkBookDto(isbn = "111", title = "Kotlin Title 1", author = "Author A", publisher = "Pub"),
            NlkBookDto(isbn = "222", title = "Kotlin Title 2", author = "Author B", publisher = "Pub"),
        )

    private val dummyAuthorBooks =
        listOf(
            NlkBookDto(isbn = "222", title = "Kotlin Title 2", author = "Author B", publisher = "Pub"), // 중복 대상
            NlkBookDto(isbn = "333", title = "Other Title", author = "Kotlin Author", publisher = "Pub"),
        )

    private val fakeBookRemoteDataSource =
        object : BookRemoteDataSource {
            override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = TODO("Not needed")

            override suspend fun searchBooksByTitle(
                query: String,
                page: Int,
                size: Int,
            ): NlkSearchResponseDto =
                if (page == 1) {
                    NlkSearchResponseDto(docs = dummyTitleBooks)
                } else {
                    NlkSearchResponseDto(docs = emptyList())
                }

            override suspend fun searchBooksByAuthor(
                query: String,
                page: Int,
                size: Int,
            ): NlkSearchResponseDto =
                if (page == 1) {
                    NlkSearchResponseDto(docs = dummyAuthorBooks)
                } else {
                    NlkSearchResponseDto(docs = emptyList())
                }
        }

    @Test
    fun load_merges_title_and_author_results_with_distinct_isbn() =
        runTest {
            val pagingSource = NlkBookPagingSource(fakeBookRemoteDataSource, "Kotlin")

            val result =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(
                        key = 1,
                        loadSize = 10,
                        placeholdersEnabled = false,
                    ),
                )

            assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
            val pageResult = result as PagingSource.LoadResult.Page<Int, com.leeseungyun1020.manicule.core.model.Book>

            // 111, 222, 333 세 권의 책이 중복 없이 반환되어야 함 (222 중복 제거)
            assertThat(pageResult.data).hasSize(3)
            assertThat(pageResult.data[0].isbn).isEqualTo("111")
            assertThat(pageResult.data[1].isbn).isEqualTo("222")
            assertThat(pageResult.data[2].isbn).isEqualTo("333")
        }

    @Test
    fun load_ends_paging_when_both_sources_are_empty() =
        runTest {
            val pagingSource = NlkBookPagingSource(fakeBookRemoteDataSource, "Kotlin")

            // 2페이지 요청 시 둘 다 결과가 비어있으므로 isEnd가 되어 nextKey가 null이어야 함
            val result =
                pagingSource.load(
                    PagingSource.LoadParams.Append(
                        key = 2,
                        loadSize = 10,
                        placeholdersEnabled = false,
                    ),
                )

            assertThat(result).isInstanceOf(PagingSource.LoadResult.Page::class.java)
            val pageResult = result as PagingSource.LoadResult.Page<Int, com.leeseungyun1020.manicule.core.model.Book>

            assertThat(pageResult.data).isEmpty()
            assertThat(pageResult.nextKey).isNull()
        }
}
