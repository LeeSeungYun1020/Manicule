package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow

interface BookEntryLocalDataSource {
    suspend fun upsert(entry: BookEntryEntity)

    suspend fun delete(isbn: String)

    fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?>

    fun observeByStatus(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    fun observeAll(): Flow<List<BookEntryWithCurrentPage>>
}
