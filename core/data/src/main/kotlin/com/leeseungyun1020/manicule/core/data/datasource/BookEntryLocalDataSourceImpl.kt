package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.BookEntryDao
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BookEntryLocalDataSourceImpl
    @Inject
    constructor(
        private val bookEntryDao: BookEntryDao,
    ) : BookEntryLocalDataSource {
        override suspend fun upsert(entry: BookEntryEntity) = bookEntryDao.upsert(entry)

        override suspend fun delete(isbn: String) = bookEntryDao.delete(isbn)

        override fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?> = bookEntryDao.observeByIsbn(isbn)

        override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>> = bookEntryDao.observeByStatus(status)

        override fun observeAll(): Flow<List<BookEntryWithCurrentPage>> = bookEntryDao.observeAll()
    }
