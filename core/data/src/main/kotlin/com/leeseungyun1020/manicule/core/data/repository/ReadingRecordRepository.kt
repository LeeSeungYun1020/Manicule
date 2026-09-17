package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

interface ReadingRecordRepository {
    /**
     * id가 0인 새 세션을 저장하고 생성된 ID를 반환한다. 책 정보는 먼저 저장되어 있어야 한다.
     * 첫 WANT 기록만 READING으로 전환하고, 등록된 책의 수정 시각을 갱신한다.
     * 미등록 책의 서재 항목은 만들지 않으며, 저장 실패 시 모든 변경을 롤백한다.
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
