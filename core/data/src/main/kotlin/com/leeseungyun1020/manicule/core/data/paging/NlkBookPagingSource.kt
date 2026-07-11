package com.leeseungyun1020.manicule.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.math.ceil

class NlkBookPagingSource(
    private val bookRemoteDataSource: BookRemoteDataSource,
    private val query: String,
) : PagingSource<Int, Book>() {

    private val request =
        mutableListOf(
            bookRemoteDataSource::searchBooksByTitle,
            bookRemoteDataSource::searchBooksByAuthor,
        )
    private val endPageList = MutableList(request.size) { Int.MAX_VALUE }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> =
        runCatching {
            val page = params.key ?: 1
            val resultList =
                coroutineScope {
                    request
                        .zip(endPageList)
                        .map { (search, endPage) ->
                            async {
                                if (page <= endPage) {
                                    search(query, page, params.loadSize)
                                } else {
                                    NlkSearchResponseDto(docs = emptyList())
                                }
                            }
                        }.awaitAll()
                }
            resultList.forEachIndexed { index, dto ->
                if (endPageList[index] == Int.MAX_VALUE) {
                    endPageList[index] =
                        dto.totalCount
                            .toDoubleOrNull()
                            ?.takeIf { it > 0.0 }
                            ?.let { ceil(it / params.loadSize).toInt() } ?: 0
                }
            }
            val books = resultList.flatMap { it.docs }.distinctBy { it.isbn }.map { it.asExternalModel() }
            LoadResult.Page(
                data = books,
                prevKey = (page - 1).takeIf { it > 0 },
                nextKey = if (endPageList.all { page >= it }) null else page + 1,
            )
        }.getOrElse { LoadResult.Error(it) }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
}
