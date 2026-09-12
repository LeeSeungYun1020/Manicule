package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.BookEntryDao
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject

class RoomBookEntryLocalDataSource
    @Inject
    constructor(
        private val bookEntryDao: BookEntryDao,
    ) : BookEntryLocalDataSource {
        override suspend fun changeReadingStatus(
            isbn: String,
            status: ReadingStatus,
            updatedAt: Instant,
            finishedAt: LocalDate?,
        ): ReadingStatusChangeResult = bookEntryDao.changeReadingStatus(isbn, status, updatedAt, finishedAt)

        override suspend fun save(entry: BookEntryEntity) = bookEntryDao.upsert(entry)

        override suspend fun remove(isbn: String) = bookEntryDao.delete(isbn)

        override fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?> = bookEntryDao.observeByIsbn(isbn)

        override fun observeByStatus(
            status: ReadingStatus,
            sort: LibrarySort,
        ): Flow<List<BookEntryWithCurrentPage>> =
            when (sort.criterion) {
                LibrarySort.Criterion.ADDED_AT ->
                    when (sort.direction) {
                        LibrarySort.Direction.ASCENDING -> bookEntryDao.observeByStatusAddedAtAscending(status)
                        LibrarySort.Direction.DESCENDING -> bookEntryDao.observeByStatusAddedAtDescending(status)
                    }
                LibrarySort.Criterion.UPDATED_AT ->
                    when (sort.direction) {
                        LibrarySort.Direction.ASCENDING -> bookEntryDao.observeByStatusUpdatedAtAscending(status)
                        LibrarySort.Direction.DESCENDING -> bookEntryDao.observeByStatusUpdatedAtDescending(status)
                    }
                LibrarySort.Criterion.RATING ->
                    when (sort.direction) {
                        LibrarySort.Direction.ASCENDING -> bookEntryDao.observeByStatusRatingAscending(status)
                        LibrarySort.Direction.DESCENDING -> bookEntryDao.observeByStatusRatingDescending(status)
                    }
            }

        override suspend fun getRecentBooksByStatus(
            status: ReadingStatus,
            limit: Int,
        ): List<BookEntity> = bookEntryDao.getRecentBooksByStatus(status, limit)

        override fun observeAll(): Flow<List<BookEntryWithCurrentPage>> = bookEntryDao.observeAll()
    }
