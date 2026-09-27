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

    /**
     * 기존 세션 기록을 갱신한다. (id > 0 이어야 함)
     * 대상 기록이 존재하고 책의 isbn이 일치하면 갱신 후 true를 반환한다.
     * 대상이 없거나 isbn이 불일치하면 false를 반환한다.
     */
    suspend fun saveRecord(record: ReadingRecord): Boolean

    /**
     * 세션 기록을 삭제한다.
     * 대상 기록이 존재하고 책의 isbn이 일치하면 삭제 후 true를 반환한다.
     * 대상이 없거나 isbn이 불일치하면 false를 반환한다.
     */
    suspend fun removeRecord(
        id: Long,
        isbn: String,
    ): Boolean

    fun observeRecordsByIsbn(isbn: String): Flow<List<ReadingRecord>>

    fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecord>>

    suspend fun getMaxEndPage(isbn: String): Int?
}
