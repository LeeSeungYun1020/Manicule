package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.datasource.BookEntryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.model.Book
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

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<Book> {
            require(limit > 0) { "limit must be positive, was $limit" }
            return bookEntryLocalDataSource.getRecentBooksByStatus(status, limit).map { it.asExternalModel() }
        }

        override fun observeBookEntry(isbn: String): Flow<BookEntry?> =
            bookEntryLocalDataSource.observeByIsbn(isbn).map { it?.asExternalModel() }

        override suspend fun saveBookEntry(entry: BookEntry) {
            bookLocalDataSource.save(entry.book.asEntity())
            bookEntryLocalDataSource.save(entry.asEntity())
        }

        override suspend fun removeBookEntry(isbn: String) {
            bookEntryLocalDataSource.remove(isbn)
        }
    }
