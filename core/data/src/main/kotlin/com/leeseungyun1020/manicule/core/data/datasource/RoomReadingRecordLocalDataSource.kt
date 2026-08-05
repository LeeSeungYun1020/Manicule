package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.ReadingRecordDao
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class RoomReadingRecordLocalDataSource
    @Inject
    constructor(
        private val readingRecordDao: ReadingRecordDao,
    ) : ReadingRecordLocalDataSource {
        override suspend fun save(record: ReadingRecordEntity): Long = readingRecordDao.upsert(record)

        override suspend fun remove(id: Long) {
            readingRecordDao.delete(id)
        }

        override fun observeByIsbn(isbn: String): Flow<List<ReadingRecordEntity>> = readingRecordDao.observeByIsbn(isbn)

        override fun observeBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecordEntity>> = readingRecordDao.observeBetween(start, end)

        override suspend fun getMaxEndPage(isbn: String): Int? = readingRecordDao.getMaxEndPage(isbn)
    }
