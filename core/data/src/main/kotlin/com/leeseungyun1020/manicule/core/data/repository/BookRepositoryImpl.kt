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

        override suspend fun getBook(isbn: String): Book? = bookDao.getByIsbn(isbn)?.asExternalModel()

        override suspend fun fetchAndCacheBook(isbn: String): Book? {
            val response = runCatching { nlkApi.searchBooks(pageNo = 1, pageSize = 1, isbn = isbn) }.getOrNull()
            val dto = response?.docs?.firstOrNull() ?: return null

            val book = dto.asExternalModel()
            bookDao.upsert(book.asEntity())
            return book
        }
    }
