package com.leeseungyun1020.manicule.core.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModelOrNull
import com.leeseungyun1020.manicule.core.data.paging.NlkBookPagingSource
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookSyncStatus
import com.leeseungyun1020.manicule.core.network.nlk.NlkContentFetchResult
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.supervisorScope
import javax.inject.Inject

class BookRepositoryImpl
    @Inject
    constructor(
        private val bookLocalDataSource: BookLocalDataSource,
        private val bookRemoteDataSource: BookRemoteDataSource,
    ) : BookRepository {

        override fun observeBook(isbn: String): Flow<Book?> = bookLocalDataSource.observeByIsbn(isbn).map { it?.asExternalModel() }

        override suspend fun syncBook(isbn: String): Result<BookSyncStatus> =
            runCatching {
                val response = bookRemoteDataSource.searchBooks(isbn = isbn)
                val mappedBook =
                    response.docs.firstNotNullOfOrNull { it.asExternalModelOrNull() }
                        ?: throw NoSuchElementException("API에서 유효한 책 정보를 찾을 수 없습니다.")

                val cached = bookLocalDataSource.getByIsbn(isbn)
                val auxiliaryContent =
                    supervisorScope {
                        val introduction =
                            async {
                                resolveAuxiliaryContent(
                                    inline = mappedBook.introduction,
                                    url = mappedBook.introductionUrl ?: mappedBook.summaryUrl,
                                    cached = cached?.introduction,
                                )
                            }
                        val tableOfContents =
                            async {
                                resolveAuxiliaryContent(
                                    inline = mappedBook.tableOfContents,
                                    url = mappedBook.tableOfContentsUrl,
                                    cached = cached?.tableOfContents,
                                )
                            }
                        AuxiliaryBookContent(
                            introduction = introduction.await(),
                            tableOfContents = tableOfContents.await(),
                        )
                    }
                val book =
                    mappedBook.copy(
                        introduction = auxiliaryContent.introduction.value,
                        tableOfContents = auxiliaryContent.tableOfContents.value,
                    )
                bookLocalDataSource.save(book.asEntity())
                if (auxiliaryContent.hasFetchFailure) {
                    BookSyncStatus.AUXILIARY_CONTENT_FAILED
                } else {
                    BookSyncStatus.COMPLETE
                }
            }.onFailure {
                Log.e("BookRepository", "Failed to sync book with ISBN $isbn", it)
            }

        override fun searchBooks(query: String): Flow<PagingData<Book>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = NETWORK_PAGE_SIZE,
                        enablePlaceholders = false,
                    ),
                pagingSourceFactory = { NlkBookPagingSource(bookRemoteDataSource, query) },
            ).flow

        private suspend fun resolveAuxiliaryContent(
            inline: String?,
            url: String?,
            cached: String?,
        ): AuxiliaryContent {
            if (inline != null || url == null) {
                return AuxiliaryContent(
                    value = inline ?: cached,
                    fetchFailed = false,
                )
            }

            val fetchResult =
                runCatching { bookRemoteDataSource.fetchNlkContent(url) }
                    .getOrDefault(NlkContentFetchResult.RetryableFailure)
            return when (fetchResult) {
                is NlkContentFetchResult.Success ->
                    AuxiliaryContent(
                        value = fetchResult.content,
                        fetchFailed = false,
                    )

                NlkContentFetchResult.Unavailable ->
                    AuxiliaryContent(
                        value = cached,
                        fetchFailed = false,
                    )

                NlkContentFetchResult.RetryableFailure ->
                    AuxiliaryContent(
                        value = cached,
                        fetchFailed = true,
                    )
            }
        }

        private data class AuxiliaryBookContent(
            val introduction: AuxiliaryContent,
            val tableOfContents: AuxiliaryContent,
        ) {
            val hasFetchFailure: Boolean
                get() = introduction.fetchFailed || tableOfContents.fetchFailed
        }

        private data class AuxiliaryContent(
            val value: String?,
            val fetchFailed: Boolean,
        )

        companion object {
            private const val NETWORK_PAGE_SIZE = 10
        }
    }
