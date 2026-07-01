package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.datasource.BookEntryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LibraryRepositoryImpl
    @Inject
    constructor(
        private val bookEntryLocalDataSource: BookEntryLocalDataSource,
        private val bookLocalDataSource: BookLocalDataSource,
    ) : LibraryRepository {

        override fun observeAll(): Flow<List<BookEntry>> =
            bookEntryLocalDataSource.observeAll().map { list ->
                list.map { it.asExternalModel() }
            }

        override fun observeByStatus(status: ReadingStatus): Flow<List<BookEntry>> =
            bookEntryLocalDataSource.observeByStatus(status).map { list ->
                list.map { it.asExternalModel() }
            }

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> =
            bookEntryLocalDataSource.observeByIsbn(isbn).map { it?.asExternalModel() }

        override suspend fun addOrUpdateBookEntry(entry: BookEntry) {
            bookLocalDataSource.upsert(entry.book.asEntity())
            bookEntryLocalDataSource.upsert(entry.asEntity())
        }

        override suspend fun deleteBookEntry(isbn: String) {
            bookEntryLocalDataSource.delete(isbn)
        }
    }
