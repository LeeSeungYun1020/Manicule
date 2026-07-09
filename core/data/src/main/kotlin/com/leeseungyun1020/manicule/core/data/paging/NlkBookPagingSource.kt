package com.leeseungyun1020.manicule.core.data.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.network.nlk.dto.NlkSearchResponseDto
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
import java.io.IOException

class NlkBookPagingSource(
    private val bookRemoteDataSource: BookRemoteDataSource,
    private val query: String,
) : PagingSource<Int, Book>() {

    private var titleEndPage = Int.MAX_VALUE
    private var authorEndPage = Int.MAX_VALUE

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Book> {
        val page = params.key ?: STARTING_PAGE_INDEX

        return try {
            supervisorScope {
                val isTitleSkipped = page >= titleEndPage
                val isAuthorSkipped = page >= authorEndPage

                val tasks =
                    listOf(
                        isTitleSkipped to
                            suspend {
                                bookRemoteDataSource.searchBooksByTitle(query = query, page = page, size = params.loadSize)
                            },
                        isAuthorSkipped to
                            suspend {
                                bookRemoteDataSource.searchBooksByAuthor(query = query, page = page, size = params.loadSize)
                            },
                    )

                val deferredList =
                    tasks.map { (isSkipped, apiCall) ->
                        async {
                            executeSearchTask(isSkipped, apiCall)
                        }
                    }

                val responses = deferredList.awaitAll()
                val titleResponse = responses[0]
                val authorResponse = responses[1]

                if (titleResponse.docs.isEmpty()) {
                    titleEndPage = minOf(titleEndPage, page)
                }
                if (authorResponse.docs.isEmpty()) {
                    authorEndPage = minOf(authorEndPage, page)
                }

                val mergedDocs =
                    (titleResponse.docs + authorResponse.docs)
                        .distinctBy { it.isbn }

                val books = mergedDocs.map { it.asExternalModel() }
                val isEnd = page >= titleEndPage && page >= authorEndPage

                LoadResult.Page(
                    data = books,
                    prevKey = if (page == STARTING_PAGE_INDEX) null else page - 1,
                    nextKey = if (isEnd) null else page + 1,
                )
            }
        } catch (exception: IOException) {
            LoadResult.Error(exception)
        }
    }

    private suspend fun executeSearchTask(
        isSkipped: Boolean,
        apiCall: suspend () -> NlkSearchResponseDto,
    ): NlkSearchResponseDto {
        if (isSkipped) return NlkSearchResponseDto(docs = emptyList())
        return try {
            apiCall()
        } catch (e: IOException) {
            Log.e("NlkBookPagingSource", "Failed to execute search task", e)
            NlkSearchResponseDto(docs = emptyList())
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Book>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }

    companion object {
        private const val STARTING_PAGE_INDEX = 1
    }
}
