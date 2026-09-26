package com.leeseungyun1020.manicule.core.data.datasource

import com.leeseungyun1020.manicule.core.database.dao.BookEntryDao
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.MemoChangeResult
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@Suppress("TooManyFunctions")
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

        override suspend fun updateRating(
            isbn: String,
            rating: Int,
            updatedAt: Instant,
        ): RatingChangeResult = bookEntryDao.updateRating(isbn, rating, updatedAt)

        override suspend fun updateMemo(
            isbn: String,
            memo: String?,
            updatedAt: Instant,
        ): MemoChangeResult = bookEntryDao.updateMemo(isbn, memo, updatedAt)

        override suspend fun save(entry: BookEntryEntity) = bookEntryDao.upsert(entry)

        override suspend fun insertIfAbsent(entry: BookEntryEntity): Boolean = bookEntryDao.insertIfAbsent(entry) != -1L

        @Suppress("LongParameterList") // Room 쿼리의 조건과 복구 열을 그대로 전달한다.
        override suspend fun restoreStatusIfUnchanged(
            isbn: String,
            changedStatus: ReadingStatus,
            changedAt: Instant,
            originalStatus: ReadingStatus,
            originalUpdatedAt: Instant,
            originalFinishedAt: LocalDate?,
        ): Boolean =
            bookEntryDao.restoreStatusIfUnchanged(
                isbn,
                changedStatus,
                changedAt,
                originalStatus,
                originalUpdatedAt,
                originalFinishedAt,
            ) == 1

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
