package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.database.dao.ReadingRecordDao
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ReadingRecordRepositoryImpl
    @Inject
    constructor(
        private val readingRecordDao: ReadingRecordDao,
    ) : ReadingRecordRepository {

        override suspend fun addOrUpdateRecord(record: ReadingRecord): Long = readingRecordDao.upsert(record.asEntity())

        override suspend fun deleteRecord(id: Long) {
            readingRecordDao.delete(id)
        }

        override fun observeRecordsByIsbn(isbn: String): Flow<List<ReadingRecord>> =
            readingRecordDao.observeByIsbn(isbn).map { list ->
                list.map {
                    it.asExternalModel()
                }
            }

        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> =
            readingRecordDao.observeBetween(start, end).map { list ->
                list.map {
                    it.asExternalModel()
                }
            }

        override suspend fun getLatestCumulativePage(isbn: String): Int? = readingRecordDao.latestCumulativeFor(isbn)
    }
