package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.BookDao
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookLocalDataSourceImpl
    @Inject
    constructor(
        private val bookDao: BookDao,
    ) : BookLocalDataSource {
        override suspend fun getByIsbn(isbn: String): BookEntity? = bookDao.getByIsbn(isbn)

        override fun observeByIsbn(isbn: String): Flow<BookEntity?> = bookDao.observeByIsbn(isbn)

        override suspend fun upsert(book: BookEntity) {
            bookDao.upsert(book)
        }
    }
