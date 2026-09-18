package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

interface ReadingRecordLocalDataSource {
    /** 새 세션 저장과 미등록·UNSET·첫 WANT 기록의 READING 등록·전환을 원자적으로 처리한다. id는 0이어야 한다. */
    suspend fun add(
        record: ReadingRecordEntity,
        updatedAt: Instant,
    ): Long

    suspend fun save(record: ReadingRecordEntity): Long

    suspend fun remove(id: Long)

    fun observeByIsbn(isbn: String): Flow<List<ReadingRecordEntity>>

    fun observeBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecordEntity>>

    suspend fun getMaxEndPage(isbn: String): Int?
}
