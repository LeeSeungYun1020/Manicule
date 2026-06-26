package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.datasource.ReadingRecordLocalDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class ReadingRecordRepositoryImpl
    @Inject
    constructor(
        private val readingRecordLocalDataSource: ReadingRecordLocalDataSource,
    ) : ReadingRecordRepository {

        override suspend fun addOrUpdateRecord(record: ReadingRecord): Long = readingRecordLocalDataSource.upsert(record.asEntity())

        override suspend fun deleteRecord(id: Long) {
            readingRecordLocalDataSource.delete(id)
        }

        override fun observeRecordsByIsbn(isbn: String): Flow<List<ReadingRecord>> =
            readingRecordLocalDataSource.observeByIsbn(isbn).map { list ->
                list.map {
                    it.asExternalModel()
                }
            }

        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> =
            readingRecordLocalDataSource.observeBetween(start, end).map { list ->
                list.map {
                    it.asExternalModel()
                }
            }

        override suspend fun getLatestCumulativePage(isbn: String): Int? = readingRecordLocalDataSource.latestCumulativeFor(isbn)
    }
