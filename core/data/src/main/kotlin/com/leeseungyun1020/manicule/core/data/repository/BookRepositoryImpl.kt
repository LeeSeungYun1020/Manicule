package com.leeseungyun1020.manicule.core.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookRemoteDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.data.paging.NlkBookPagingSource
import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookRepositoryImpl
    @Inject
    constructor(
        private val bookLocalDataSource: BookLocalDataSource,
        private val bookRemoteDataSource: BookRemoteDataSource,
    ) : BookRepository {

        override fun observeBook(isbn: String): Flow<Book?> = bookLocalDataSource.observeByIsbn(isbn).map { it?.asExternalModel() }

        override suspend fun syncBook(isbn: String): Result<Unit> =
            runCatching {
                val response = bookRemoteDataSource.searchBooks(isbn = isbn)
                val dto = response.docs.firstOrNull() ?: throw NoSuchElementException("API에서 해당 ISBN의 책을 찾을 수 없습니다.")

                val book = dto.asExternalModel()
                bookLocalDataSource.save(book.asEntity())
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

        companion object {
            private const val NETWORK_PAGE_SIZE = 10
        }
    }
