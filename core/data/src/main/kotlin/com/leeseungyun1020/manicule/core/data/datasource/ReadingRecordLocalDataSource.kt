package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ReadingRecordLocalDataSource {
    suspend fun upsert(record: ReadingRecordEntity): Long

    suspend fun delete(id: Long)

    fun observeByIsbn(isbn: String): Flow<List<ReadingRecordEntity>>

    fun observeBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecordEntity>>

    suspend fun latestCumulativeFor(isbn: String): Int?
}
