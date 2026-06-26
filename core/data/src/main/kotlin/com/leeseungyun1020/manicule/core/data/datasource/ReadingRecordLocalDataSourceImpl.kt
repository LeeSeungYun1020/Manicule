package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.ReadingRecordDao
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ReadingRecordLocalDataSourceImpl
    @Inject
    constructor(
        private val readingRecordDao: ReadingRecordDao,
    ) : ReadingRecordLocalDataSource {
        override suspend fun upsert(record: ReadingRecordEntity): Long = readingRecordDao.upsert(record)

        override suspend fun delete(id: Long) {
            readingRecordDao.delete(id)
        }

        override fun observeByIsbn(isbn: String): Flow<List<ReadingRecordEntity>> = readingRecordDao.observeByIsbn(isbn)

        override fun observeBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecordEntity>> = readingRecordDao.observeBetween(start, end)

        override suspend fun latestCumulativeFor(isbn: String): Int? = readingRecordDao.latestCumulativeFor(isbn)
    }
