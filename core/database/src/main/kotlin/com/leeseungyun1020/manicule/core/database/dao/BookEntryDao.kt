package com.leeseungyun1020.manicule.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

@Dao
@Suppress("TooManyFunctions")
interface BookEntryDao {
    /** 존재 확인, 최초 등록, 상태 변경을 하나의 트랜잭션으로 처리한다. */
    @Transaction
    @Suppress("ReturnCount") // 상태별 조기 반환으로 트랜잭션의 쓰기 경로를 구분한다.
    suspend fun changeReadingStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Instant,
        finishedAt: LocalDate?,
    ): ReadingStatusChangeResult {
        if (status == ReadingStatus.UNSET) return ReadingStatusChangeResult.InvalidStatus
        require((status == ReadingStatus.FINISHED) == (finishedAt != null))
        if (!bookExists(isbn)) return ReadingStatusChangeResult.BookNotFound
        val entry = getEntry(isbn)
        if (entry?.status == status) return ReadingStatusChangeResult.Unchanged
        if (entry == null) {
            upsert(BookEntryEntity(isbn, status, 0, null, updatedAt, updatedAt, finishedAt))
        } else {
            updateStatus(isbn, status, updatedAt, finishedAt)
        }
        return ReadingStatusChangeResult.Changed
    }

    /** 존재 확인, 최초 등록, 별점 변경을 하나의 트랜잭션으로 처리한다. */
    @Transaction
    @Suppress("ReturnCount") // 상태별 조기 반환으로 트랜잭션의 쓰기 경로를 구분한다.
    suspend fun updateRating(
        isbn: String,
        rating: Int,
        updatedAt: Instant,
    ): RatingChangeResult {
        if (rating !in MIN_RATING..MAX_RATING) return RatingChangeResult.InvalidRating
        if (!bookExists(isbn)) return RatingChangeResult.BookNotFound
        val entry = getEntry(isbn)
        if (entry == null) {
            if (rating == 0) return RatingChangeResult.Unchanged
            upsert(BookEntryEntity(isbn, ReadingStatus.UNSET, rating, null, updatedAt, updatedAt, null))
            return RatingChangeResult.Changed
        }
        if (entry.rating == rating) return RatingChangeResult.Unchanged
        setRating(isbn, rating, updatedAt)
        return RatingChangeResult.Changed
    }

    @Query("SELECT EXISTS(SELECT 1 FROM books WHERE isbn = :isbn)")
    suspend fun bookExists(isbn: String): Boolean

    @Query("SELECT * FROM book_entries WHERE isbn = :isbn")
    suspend fun getEntry(isbn: String): BookEntryEntity?

    @Query("UPDATE book_entries SET status = :status, updatedAt = :updatedAt, finishedAt = :finishedAt WHERE isbn = :isbn")
    suspend fun updateStatus(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Instant,
        finishedAt: LocalDate?,
    )

    @Query("UPDATE book_entries SET rating = :rating, updatedAt = :updatedAt WHERE isbn = :isbn")
    suspend fun setRating(
        isbn: String,
        rating: Int,
        updatedAt: Instant,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: BookEntryEntity)

    /** 삭제 이후 다시 등록된 항목은 보존하고, 캐시된 책 정보는 건드리지 않는다. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(entry: BookEntryEntity): Long

    /** 상태 변경 이후 다른 수정이 없을 때만 그 변경이 건드린 열을 되돌린다. */
    @Query(
        """
        UPDATE book_entries
        SET status = :originalStatus, updatedAt = :originalUpdatedAt, finishedAt = :originalFinishedAt
        WHERE isbn = :isbn AND status = :changedStatus AND updatedAt = :changedAt
        """,
    )
    @Suppress("LongParameterList") // 조건부 UPDATE의 각 열을 SQL 바인딩으로 전달한다.
    suspend fun restoreStatusIfUnchanged(
        isbn: String,
        changedStatus: ReadingStatus,
        changedAt: Instant,
        originalStatus: ReadingStatus,
        originalUpdatedAt: Instant,
        originalFinishedAt: LocalDate?,
    ): Int

    @Query("DELETE FROM book_entries WHERE isbn = :isbn")
    suspend fun delete(isbn: String)

    @Transaction
    @Query(
        value = """
            SELECT *, 
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries 
            WHERE isbn = :isbn
        """,
    )
    fun observeByIsbn(isbn: String): Flow<BookEntryWithCurrentPage?>

    @Transaction
    @Query(
        value = """
            SELECT *, 
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries 
            WHERE status = :status
            ORDER BY addedAt ASC
        """,
    )
    fun observeByStatusAddedAtAscending(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    @Transaction
    @Query(
        value = """
            SELECT *,
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries
            WHERE status = :status
            ORDER BY addedAt DESC
        """,
    )
    fun observeByStatusAddedAtDescending(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    @Transaction
    @Query(
        value = """
            SELECT *,
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries
            WHERE status = :status
            ORDER BY updatedAt ASC
        """,
    )
    fun observeByStatusUpdatedAtAscending(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    @Transaction
    @Query(
        value = """
            SELECT *,
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries
            WHERE status = :status
            ORDER BY updatedAt DESC
        """,
    )
    fun observeByStatusUpdatedAtDescending(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    @Transaction
    @Query(
        value = """
            SELECT *,
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries
            WHERE status = :status
            ORDER BY rating ASC, updatedAt DESC
        """,
    )
    fun observeByStatusRatingAscending(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    @Transaction
    @Query(
        value = """
            SELECT *,
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries
            WHERE status = :status
            ORDER BY rating DESC, updatedAt DESC
        """,
    )
    fun observeByStatusRatingDescending(status: ReadingStatus): Flow<List<BookEntryWithCurrentPage>>

    @Query(
        value = """
            SELECT books.*
            FROM book_entries
            INNER JOIN books ON books.isbn = book_entries.isbn
            WHERE book_entries.status = :status
            ORDER BY book_entries.updatedAt DESC, book_entries.isbn ASC
            LIMIT :limit
        """,
    )
    suspend fun getRecentBooksByStatus(
        status: ReadingStatus,
        limit: Int,
    ): List<BookEntity>

    @Transaction
    @Query(
        value = """
            SELECT *, 
            (SELECT MAX(endPage) FROM reading_records WHERE isbn = book_entries.isbn) AS currentPage
            FROM book_entries
            ORDER BY updatedAt DESC, isbn ASC
        """,
    )
    fun observeAll(): Flow<List<BookEntryWithCurrentPage>>
}

private const val MIN_RATING = 0
private const val MAX_RATING = 5
