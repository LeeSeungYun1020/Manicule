package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

interface BookEntryLocalDataSource {
    suspend fun changeReadingStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Instant,
        finishedAt: LocalDate?,
    ): ReadingStatusChangeResult

    suspend fun save(entry: BookEntryEntity)

    suspend fun remove(isbn: String)

    fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?>

    fun observeByStatus(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<BookEntity>

    fun observeAll(): Flow<List<BookEntryWithCurrentPage>>
}
