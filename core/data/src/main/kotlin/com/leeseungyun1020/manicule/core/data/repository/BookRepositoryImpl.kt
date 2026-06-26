package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.database.dao.BookDao
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.network.nlk.NlkApi
import javax.inject.Inject

class BookRepositoryImpl
    @Inject
    constructor(
        private val bookDao: BookDao,
        private val nlkApi: NlkApi,
    ) : BookRepository {

        override suspend fun getBook(isbn: String): Result<Book> =
            runCatching {
                bookDao.getByIsbn(isbn)?.asExternalModel()
                    ?: throw NoSuchElementException("해당 책 정보를 로컬에서 찾을 수 없습니다.")
            }

        override suspend fun fetchAndCacheBook(isbn: String): Result<Book> =
            runCatching {
                val response = nlkApi.searchBooks(pageNo = 1, pageSize = 1, isbn = isbn)
                val dto = response.docs.firstOrNull() ?: throw NoSuchElementException("API에서 해당 ISBN의 책을 찾을 수 없습니다.")

                val book = dto.asExternalModel()
                bookDao.upsert(book.asEntity())
                book
            }
    }
