package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

interface ReadingRecordRepository {
    /**
     * 새 세션을 저장하고 생성된 ID를 반환한다. (id는 0이어야 함)
     * 미등록 책은 READING으로 등록하고 UNSET·첫 WANT 기록은 READING으로 전환한다.
     * 저장 실패 시 모든 변경을 롤백한다.
     */
    suspend fun addRecord(
        record: ReadingRecord,
        updatedAt: Instant,
    ): Long

    suspend fun saveRecord(record: ReadingRecord): Long

    suspend fun removeRecord(id: Long)

    fun observeRecordsByIsbn(isbn: String): Flow<List<ReadingRecord>>

    fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecord>>

    suspend fun getMaxEndPage(isbn: String): Int?
}
