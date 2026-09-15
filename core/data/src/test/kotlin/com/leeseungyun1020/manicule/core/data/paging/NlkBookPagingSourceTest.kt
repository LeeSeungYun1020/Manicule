package com.leeseungyun1020.manicule.core.data.paging

import androidx.paging.PagingSource
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSource
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkBookDto
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

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
                    NlkSearchResponseDto(totalCount = "2", docs = dummyTitleBooks)
                } else {
                    NlkSearchResponseDto(totalCount = "2", docs = emptyList())
                }

            override suspend fun searchBooksByAuthor(
                query: String,
                page: Int,
                size: Int,
            ): NlkSearchResponseDto =
                if (page == 1) {
                    NlkSearchResponseDto(totalCount = "2", docs = dummyAuthorBooks)
                } else {
                    NlkSearchResponseDto(totalCount = "2", docs = emptyList())
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
            // totalCount=2, loadSize=10 → endPage=1이므로 nextKey=null
            assertThat(pageResult.nextKey).isNull()
        }

    @Test
    fun load_excludesBooksWithBlankRequiredFields() =
        runTest {
            val mixedDataSource =
                object : BookRemoteDataSource {
                    override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = TODO("Not needed")

                    override suspend fun searchBooksByTitle(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto =
                        NlkSearchResponseDto(
                            totalCount = "3",
                            docs =
                                listOf(
                                    NlkBookDto(isbn = "123", title = "Valid Book"),
                                    NlkBookDto(isbn = " ", title = "Blank ISBN"),
                                    NlkBookDto(isbn = "456", title = " "),
                                ),
                        )

                    override suspend fun searchBooksByAuthor(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto = NlkSearchResponseDto(totalCount = "0")
                }
            val pagingSource = NlkBookPagingSource(mixedDataSource, "Book")

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
            assertThat(pageResult.data.map { it.isbn }).containsExactly("123")
        }

    @Test
    fun load_ends_paging_when_both_sources_are_empty() =
        runTest {
            val pagingSource = NlkBookPagingSource(fakeBookRemoteDataSource, "Kotlin")

            // 1페이지 로드하여 endPageList 초기화
            pagingSource.load(
                PagingSource.LoadParams.Refresh(key = 1, loadSize = 10, placeholdersEnabled = false),
            )

            // 2페이지 요청 시 endPage(1)을 초과하므로 API 호출 없이 빈 결과
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

    @Test
    fun load_returns_error_on_network_failure() =
        runTest {
            val errorDataSource =
                object : BookRemoteDataSource {
                    override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = TODO("Not needed")

                    override suspend fun searchBooksByTitle(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto = throw IOException("Network error")

                    override suspend fun searchBooksByAuthor(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto = NlkSearchResponseDto(totalCount = "1", docs = emptyList())
                }
            val pagingSource = NlkBookPagingSource(errorDataSource, "Kotlin")

            val result =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = 1, loadSize = 10, placeholdersEnabled = false),
                )

            assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
        }

    @Test
    fun load_calculates_end_page_from_total_count() =
        runTest {
            val multiPageDataSource =
                object : BookRemoteDataSource {
                    override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = TODO("Not needed")

                    override suspend fun searchBooksByTitle(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto =
                        NlkSearchResponseDto(
                            totalCount = "5",
                            docs =
                                if (page <= 3) {
                                    listOf(NlkBookDto(isbn = "T$page", title = "T", author = "A", publisher = "P"))
                                } else {
                                    emptyList()
                                },
                        )

                    override suspend fun searchBooksByAuthor(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto =
                        NlkSearchResponseDto(
                            totalCount = "3",
                            docs =
                                if (page <= 2) {
                                    listOf(NlkBookDto(isbn = "A$page", title = "A", author = "A", publisher = "P"))
                                } else {
                                    emptyList()
                                },
                        )
                }
            val pagingSource = NlkBookPagingSource(multiPageDataSource, "query", pageSize = 2)

            // loadSize=2 → title endPage=ceil(5/2)=3, author endPage=ceil(3/2)=2
            val page1 =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = 1, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            assertThat(page1.nextKey).isEqualTo(2)

            val page2 =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = 2, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            // page=2, title endPage=3 → 계속, author endPage=2 → 마지막
            assertThat(page2.nextKey).isEqualTo(3)

            val page3 =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = 3, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            // page=3, title endPage=3 → 마지막, author endPage=2 → 이미 종료
            assertThat(page3.nextKey).isNull()
        }

    @Test
    fun load_skips_completed_source() =
        runTest {
            var titleCallCount = 0
            var authorCallCount = 0
            val trackingDataSource =
                object : BookRemoteDataSource {
                    override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = TODO("Not needed")

                    override suspend fun searchBooksByTitle(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto {
                        titleCallCount++
                        return NlkSearchResponseDto(
                            totalCount = "4",
                            docs = listOf(NlkBookDto(isbn = "T$page", title = "T", author = "A", publisher = "P")),
                        )
                    }

                    override suspend fun searchBooksByAuthor(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto {
                        authorCallCount++
                        return NlkSearchResponseDto(
                            totalCount = "1",
                            docs = listOf(NlkBookDto(isbn = "A$page", title = "A", author = "A", publisher = "P")),
                        )
                    }
                }
            val pagingSource = NlkBookPagingSource(trackingDataSource, "query", pageSize = 2)

            // loadSize=2 → title endPage=ceil(4/2)=2, author endPage=ceil(1/2)=1
            pagingSource.load(
                PagingSource.LoadParams.Refresh(key = 1, loadSize = 2, placeholdersEnabled = false),
            )
            assertThat(titleCallCount).isEqualTo(1)
            assertThat(authorCallCount).isEqualTo(1)

            // page=2: title 호출됨, author는 endPage(1) 초과이므로 건너뜀
            pagingSource.load(
                PagingSource.LoadParams.Append(key = 2, loadSize = 2, placeholdersEnabled = false),
            )
            assertThat(titleCallCount).isEqualTo(2)
            assertThat(authorCallCount).isEqualTo(1) // 호출되지 않음
        }

    @Test
    fun load_usesFixedPageSizeRegardlessOfLoadSizeInParams() =
        runTest {
            val requestedSizes = mutableListOf<Int>()
            val trackingDataSource =
                object : BookRemoteDataSource {
                    override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = TODO("Not needed")

                    override suspend fun searchBooksByTitle(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto {
                        requestedSizes += size
                        return NlkSearchResponseDto(totalCount = "0")
                    }

                    override suspend fun searchBooksByAuthor(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto {
                        requestedSizes += size
                        return NlkSearchResponseDto(totalCount = "0")
                    }
                }

            val pagingSource = NlkBookPagingSource(trackingDataSource, "query", pageSize = 10)
            pagingSource.load(
                PagingSource.LoadParams.Refresh(key = 1, loadSize = 30, placeholdersEnabled = false),
            )

            // loadParams.loadSize가 30이어도 NlkBookPagingSource는 고정 pageSize인 10으로 API를 호출해야 함
            assertThat(requestedSizes).containsExactly(10, 10)
        }

    @Test
    fun load_deduplicatesIsbnsAcrossMultiplePages() =
        runTest {
            val crossPageDataSource =
                object : BookRemoteDataSource {
                    override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = TODO("Not needed")

                    override suspend fun searchBooksByTitle(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto =
                        when (page) {
                            1 ->
                                NlkSearchResponseDto(
                                    totalCount = "4",
                                    docs =
                                        listOf(
                                            NlkBookDto(isbn = "111", title = "Book 1"),
                                            NlkBookDto(isbn = "222", title = "Book 2"),
                                        ),
                                )
                            2 ->
                                NlkSearchResponseDto(
                                    totalCount = "4",
                                    docs =
                                        listOf(
                                            NlkBookDto(isbn = "222", title = "Book 2 Duplicate"),
                                            NlkBookDto(isbn = "333", title = "Book 3"),
                                        ),
                                )
                            else -> NlkSearchResponseDto(totalCount = "4", docs = emptyList())
                        }

                    override suspend fun searchBooksByAuthor(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto = NlkSearchResponseDto(totalCount = "0")
                }

            val pagingSource = NlkBookPagingSource(crossPageDataSource, "query", pageSize = 2)

            val page1 =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = 1, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            assertThat(page1.data.map { it.isbn }).containsExactly("111", "222").inOrder()

            val page2 =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = 2, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            // 이전 페이지에서 이미 로드된 "222"는 제외되고 "333"만 반환되어야 함
            assertThat(page2.data.map { it.isbn }).containsExactly("333")
        }

    @Test
    fun load_withInitialLoadSizeDifferentFromPageSizeDoesNotDuplicateOrSkip() =
        runTest {
            val sequentialDataSource =
                object : BookRemoteDataSource {
                    override suspend fun searchBooks(isbn: String): NlkSearchResponseDto = TODO("Not needed")

                    override suspend fun searchBooksByTitle(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto {
                        val first = (page - 1) * size + 1
                        val last = minOf(first + size - 1, 6)
                        return NlkSearchResponseDto(
                            totalCount = "6",
                            docs = (first..last).map { NlkBookDto(isbn = "isbn-$it", title = "Book $it") },
                        )
                    }

                    override suspend fun searchBooksByAuthor(
                        query: String,
                        page: Int,
                        size: Int,
                    ): NlkSearchResponseDto = NlkSearchResponseDto(totalCount = "0")
                }

            val pagingSource = NlkBookPagingSource(sequentialDataSource, "query", pageSize = 2)

            // Refresh 요청 크기가 6이어도 pageSize(2) 기준으로 1페이지(1..2) 로드
            val page1 =
                pagingSource.load(
                    PagingSource.LoadParams.Refresh(key = 1, loadSize = 6, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            assertThat(page1.data.map { it.isbn }).containsExactly("isbn-1", "isbn-2")
            assertThat(page1.nextKey).isEqualTo(2)

            // Append 요청 크기가 2일 때 2페이지(3..4) 로드
            val page2 =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = 2, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            assertThat(page2.data.map { it.isbn }).containsExactly("isbn-3", "isbn-4")
            assertThat(page2.nextKey).isEqualTo(3)

            // Append 3페이지(5..6) 로드 후 종료
            val page3 =
                pagingSource.load(
                    PagingSource.LoadParams.Append(key = 3, loadSize = 2, placeholdersEnabled = false),
                ) as PagingSource.LoadResult.Page
            assertThat(page3.data.map { it.isbn }).containsExactly("isbn-5", "isbn-6")
            assertThat(page3.nextKey).isNull()
        }

    @Test
    fun init_withNonPositivePageSize_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException::class.java) {
            NlkBookPagingSource(fakeBookRemoteDataSource, "query", pageSize = 0)
        }
        assertThrows(IllegalArgumentException::class.java) {
            NlkBookPagingSource(fakeBookRemoteDataSource, "query", pageSize = -1)
        }
    }
}
