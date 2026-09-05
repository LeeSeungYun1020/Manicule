package com.leeseungyun1020.manicule.core.data.repository

import com.leeseungyun1020.manicule.core.data.datasource.BookEntryLocalDataSource
import com.leeseungyun1020.manicule.core.data.datasource.BookLocalDataSource
import com.leeseungyun1020.manicule.core.data.mapper.asEntity
import com.leeseungyun1020.manicule.core.data.mapper.asExternalModel
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class LibraryRepositoryImpl
    @Inject
    constructor(
        private val bookEntryLocalDataSource: BookEntryLocalDataSource,
        private val bookLocalDataSource: BookLocalDataSource,
    ) : LibraryRepository {
        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: LocalDate?,
        ): ReadingStatusChangeResult = bookEntryLocalDataSource.changeReadingStatus(isbn, status, updatedAt, finishedAt)

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

        override suspend fun saveBookEntry(entry: BookEntry): SaveBookEntryResult {
            if (entry.rating !in 0..5) {
                return SaveBookEntryResult.InvalidRating(entry.rating)
            }

            bookLocalDataSource.save(entry.book.asEntity())
            bookEntryLocalDataSource.save(entry.asEntity())
            return SaveBookEntryResult.Saved
        }

        override suspend fun removeBookEntry(isbn: String) {
            bookEntryLocalDataSource.remove(isbn)
        }
    }
