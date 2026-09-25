package com.leeseungyun1020.manicule.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.ManiculeDatabase
import com.leeseungyun1020.manicule.core.database.dao.projection.BookEntryWithCurrentPage
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import com.leeseungyun1020.manicule.core.model.RatingChangeResult
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test

class BookEntryDaoTest {
    private lateinit var db: ManiculeDatabase
    private lateinit var dao: BookEntryDao
    private lateinit var bookDao: BookDao
    private lateinit var recordDao: ReadingRecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ManiculeDatabase::class.java).build()
        dao = db.bookEntryDao()
        bookDao = db.bookDao()
        recordDao = db.readingRecordDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun currentPage_is_highest_end_page_even_if_latest_record_is_lower() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
            val entry =
                BookEntryEntity(
                    isbn,
                    ReadingStatus.READING,
                    0,
                    null,
                    Instant.fromEpochMilliseconds(0),
                    Instant.fromEpochMilliseconds(0),
                    null,
                )
            dao.upsert(entry)

            recordDao.upsert(
                ReadingRecordEntity(
                    isbn = isbn,
                    date = LocalDate(2024, 1, 1),
                    time = LocalTime(10, 0),
                    startPage = 80,
                    endPage = 100,
                ),
            )
            recordDao.upsert(
                ReadingRecordEntity(
                    isbn = isbn,
                    date = LocalDate(2024, 1, 2),
                    time = LocalTime(10, 0),
                    startPage = 1,
                    endPage = 50,
                ),
            )

            dao.observeByIsbn(isbn).test {
                val result = awaitItem()
                assertThat(result?.currentPage).isEqualTo(100)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun delete_removes_entry() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
            dao.upsert(
                BookEntryEntity(
                    isbn,
                    ReadingStatus.READING,
                    0,
                    null,
                    Instant.fromEpochMilliseconds(0),
                    Instant.fromEpochMilliseconds(0),
                    null,
                ),
            )

            dao.delete(isbn)
            dao.observeByIsbn(isbn).test {
                assertThat(awaitItem()).isNull()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getRecentBooksByStatus_returnsFiveMostRecentlyUpdatedMatchingBooks() =
        runTest {
            (1..6).forEach { index ->
                saveBookEntry(
                    isbn = "reading-$index",
                    status = ReadingStatus.READING,
                    updatedAt = Instant.fromEpochMilliseconds(index.toLong()),
                )
            }
            saveBookEntry(
                isbn = "finished",
                status = ReadingStatus.FINISHED,
                updatedAt = Instant.fromEpochMilliseconds(100),
            )

            val books = dao.getRecentBooksByStatus(ReadingStatus.READING, limit = 5)

            assertThat(books.map { it.isbn })
                .containsExactly("reading-6", "reading-5", "reading-4", "reading-3", "reading-2")
                .inOrder()
        }

    private suspend fun saveBookEntry(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Instant,
        addedAt: Instant = updatedAt,
        rating: Int = 0,
    ) {
        bookDao.upsert(
            BookEntity(
                isbn = isbn,
                title = isbn,
                author = "Author",
                publisher = "Publisher",
                publishedDate = null,
                coverUrl = null,
                totalPages = null,
                price = null,
                category = null,
                tableOfContentsUrl = null,
                introductionUrl = null,
                summaryUrl = null,
            ),
        )
        dao.upsert(
            BookEntryEntity(
                isbn = isbn,
                status = status,
                rating = rating,
                memo = null,
                addedAt = addedAt,
                updatedAt = updatedAt,
                finishedAt = null,
            ),
        )
    }

    @Test
    fun observeByStatus_filtersByStatus() =
        runTest {
            saveBookEntry("9783", ReadingStatus.READING, Instant.fromEpochMilliseconds(10))
            saveBookEntry("9782", ReadingStatus.WANT, Instant.fromEpochMilliseconds(20))
            saveBookEntry("9781", ReadingStatus.WANT, Instant.fromEpochMilliseconds(20))

            dao.observeByStatusUpdatedAtDescending(ReadingStatus.WANT).test {
                assertThat(awaitItem().map { it.entry.isbn }).containsExactly("9781", "9782")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observeByStatus_supportsEverySortOption() =
        runTest {
            saveBookEntry(
                isbn = "book-a",
                status = ReadingStatus.WANT,
                addedAt = Instant.fromEpochMilliseconds(10),
                updatedAt = Instant.fromEpochMilliseconds(40),
                rating = 2,
            )
            saveBookEntry(
                isbn = "book-b",
                status = ReadingStatus.WANT,
                addedAt = Instant.fromEpochMilliseconds(20),
                updatedAt = Instant.fromEpochMilliseconds(30),
                rating = 5,
            )
            saveBookEntry(
                isbn = "book-c",
                status = ReadingStatus.WANT,
                addedAt = Instant.fromEpochMilliseconds(30),
                updatedAt = Instant.fromEpochMilliseconds(20),
                rating = 5,
            )
            saveBookEntry(
                isbn = "book-d",
                status = ReadingStatus.WANT,
                addedAt = Instant.fromEpochMilliseconds(40),
                updatedAt = Instant.fromEpochMilliseconds(10),
                rating = 0,
            )

            assertOrder(
                dao.observeByStatusAddedAtAscending(ReadingStatus.WANT),
                "book-a",
                "book-b",
                "book-c",
                "book-d",
            )
            assertOrder(
                dao.observeByStatusAddedAtDescending(ReadingStatus.WANT),
                "book-d",
                "book-c",
                "book-b",
                "book-a",
            )
            assertOrder(
                dao.observeByStatusUpdatedAtAscending(ReadingStatus.WANT),
                "book-d",
                "book-c",
                "book-b",
                "book-a",
            )
            assertOrder(
                dao.observeByStatusUpdatedAtDescending(ReadingStatus.WANT),
                "book-a",
                "book-b",
                "book-c",
                "book-d",
            )
            assertOrder(
                dao.observeByStatusRatingAscending(ReadingStatus.WANT),
                "book-d",
                "book-a",
                "book-b",
                "book-c",
            )
            assertOrder(
                dao.observeByStatusRatingDescending(ReadingStatus.WANT),
                "book-b",
                "book-c",
                "book-a",
                "book-d",
            )
        }

    private suspend fun assertOrder(
        flow: Flow<List<BookEntryWithCurrentPage>>,
        vararg expectedIsbns: String,
    ) {
        flow.test {
            assertThat(awaitItem().map { it.entry.isbn }).containsExactly(*expectedIsbns).inOrder()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun changeStatus_registersCachedBookInEachLibraryTab() =
        runTest {
            val now = Instant.parse("2026-09-05T01:00:00Z")
            listOf(ReadingStatus.WANT, ReadingStatus.READING, ReadingStatus.FINISHED).forEach { status ->
                val isbn = status.name
                bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
                val date = if (status == ReadingStatus.FINISHED) LocalDate(2026, 9, 5) else null
                assertThat(dao.changeReadingStatus(isbn, status, now, date)).isEqualTo(ReadingStatusChangeResult.Changed)
                assertThat(dao.getEntry(isbn)).isEqualTo(BookEntryEntity(isbn, status, 0, null, now, now, date))
            }
        }

    @Test
    fun changeStatus_preservesReviewBookAndRecords_andHandlesRereading() =
        runTest {
            val initialTime = Instant.fromEpochMilliseconds(1)
            saveBookEntry("123", ReadingStatus.UNSET, initialTime)
            val original = checkNotNull(dao.getEntry("123")).copy(rating = 4, memo = "Keep review")
            dao.upsert(original)
            val book = bookDao.getByIsbn("123")
            recordDao.upsert(
                ReadingRecordEntity(isbn = "123", date = LocalDate(2026, 9, 1), time = LocalTime(10, 0), startPage = 1, endPage = 42),
            )
            val finishedTime = Instant.parse("2026-09-05T01:00:00Z")
            val finishedDate = LocalDate(2026, 9, 5)

            dao.changeReadingStatus("123", ReadingStatus.FINISHED, finishedTime, finishedDate)
            assertThat(
                dao.getEntry("123"),
            ).isEqualTo(original.copy(status = ReadingStatus.FINISHED, updatedAt = finishedTime, finishedAt = finishedDate))
            val later = Instant.parse("2026-09-06T01:00:00Z")
            assertThat(
                dao.changeReadingStatus("123", ReadingStatus.FINISHED, later, LocalDate(2026, 9, 6)),
            ).isEqualTo(ReadingStatusChangeResult.Unchanged)
            assertThat(dao.getEntry("123")?.finishedAt).isEqualTo(finishedDate)
            assertThat(dao.getEntry("123")?.updatedAt).isEqualTo(finishedTime)

            dao.changeReadingStatus("123", ReadingStatus.READING, later, null)
            assertThat(dao.getEntry("123")).isEqualTo(original.copy(status = ReadingStatus.READING, updatedAt = later))
            assertThat(bookDao.getByIsbn("123")).isEqualTo(book)
            dao.observeByIsbn("123").test {
                assertThat(awaitItem()?.currentPage).isEqualTo(42)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun changeStatus_rejectsMissingBookAndUnset_withoutWriting() =
        runTest {
            val now = Instant.fromEpochMilliseconds(1)
            assertThat(dao.changeReadingStatus("missing", ReadingStatus.WANT, now, null)).isEqualTo(ReadingStatusChangeResult.BookNotFound)
            assertThat(dao.getEntry("missing")).isNull()
            saveBookEntry("123", ReadingStatus.READING, now)
            val before = dao.getEntry("123")
            assertThat(dao.changeReadingStatus("123", ReadingStatus.UNSET, now, null)).isEqualTo(ReadingStatusChangeResult.InvalidStatus)
            assertThat(dao.getEntry("123")).isEqualTo(before)
        }

    @Test
    fun updateRating_unregisteredBook_setsUnsetWithRating_whenRatingIsPositive() =
        runTest {
            val isbn = "unregistered"
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
            val now = Instant.parse("2026-09-05T01:00:00Z")

            val result = dao.updateRating(isbn, 4, now)

            assertThat(result).isEqualTo(RatingChangeResult.Changed)
            assertThat(dao.getEntry(isbn)).isEqualTo(
                BookEntryEntity(
                    isbn = isbn,
                    status = ReadingStatus.UNSET,
                    rating = 4,
                    memo = null,
                    addedAt = now,
                    updatedAt = now,
                    finishedAt = null,
                ),
            )
        }

    @Test
    fun updateRating_unregisteredBook_zeroRating_returnsUnchanged_withoutWriting() =
        runTest {
            val isbn = "unregistered"
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
            val now = Instant.parse("2026-09-05T01:00:00Z")

            val result = dao.updateRating(isbn, 0, now)

            assertThat(result).isEqualTo(RatingChangeResult.Unchanged)
            assertThat(dao.getEntry(isbn)).isNull()
        }

    @Test
    fun updateRating_preservesExistingFieldsAndFinishedDate() =
        runTest {
            val isbn = "123"
            val t1 = Instant.fromEpochMilliseconds(10)
            val t2 = Instant.fromEpochMilliseconds(20)
            val t3 = Instant.fromEpochMilliseconds(30)
            val finishDate = LocalDate(2026, 9, 1)
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
            val original =
                BookEntryEntity(
                    isbn = isbn,
                    status = ReadingStatus.FINISHED,
                    rating = 3,
                    memo = "Great book",
                    addedAt = t1,
                    updatedAt = t2,
                    finishedAt = finishDate,
                )
            dao.upsert(original)

            val result = dao.updateRating(isbn, 5, t3)

            assertThat(result).isEqualTo(RatingChangeResult.Changed)
            assertThat(dao.getEntry(isbn)).isEqualTo(
                original.copy(rating = 5, updatedAt = t3),
            )
        }

    @Test
    fun updateRating_sameRating_returnsUnchanged_andPreservesUpdatedAt() =
        runTest {
            val isbn = "123"
            val t1 = Instant.fromEpochMilliseconds(10)
            val t2 = Instant.fromEpochMilliseconds(20)
            val t3 = Instant.fromEpochMilliseconds(30)
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
            dao.upsert(
                BookEntryEntity(
                    isbn = isbn,
                    status = ReadingStatus.READING,
                    rating = 4,
                    memo = "Memo",
                    addedAt = t1,
                    updatedAt = t2,
                    finishedAt = null,
                ),
            )

            val result = dao.updateRating(isbn, 4, t3)

            assertThat(result).isEqualTo(RatingChangeResult.Unchanged)
            assertThat(dao.getEntry(isbn)?.updatedAt).isEqualTo(t2)
        }

    @Test
    fun updateRating_zeroRating_clearsRating_andUpdatesTimestamp() =
        runTest {
            val isbn = "123"
            val t1 = Instant.fromEpochMilliseconds(10)
            val t2 = Instant.fromEpochMilliseconds(20)
            val t3 = Instant.fromEpochMilliseconds(30)
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
            dao.upsert(
                BookEntryEntity(
                    isbn = isbn,
                    status = ReadingStatus.READING,
                    rating = 4,
                    memo = "Memo",
                    addedAt = t1,
                    updatedAt = t2,
                    finishedAt = null,
                ),
            )

            val result = dao.updateRating(isbn, 0, t3)

            assertThat(result).isEqualTo(RatingChangeResult.Changed)
            val updated = dao.getEntry(isbn)
            assertThat(updated?.rating).isEqualTo(0)
            assertThat(updated?.updatedAt).isEqualTo(t3)
            assertThat(updated?.status).isEqualTo(ReadingStatus.READING)
            assertThat(updated?.memo).isEqualTo("Memo")
        }

    @Test
    fun updateRating_missingBook_returnsBookNotFound_withoutWriting() =
        runTest {
            val now = Instant.fromEpochMilliseconds(10)
            val result = dao.updateRating("missing", 4, now)

            assertThat(result).isEqualTo(RatingChangeResult.BookNotFound)
            assertThat(dao.getEntry("missing")).isNull()
        }

    @Test
    fun updateRating_invalidRating_returnsInvalidRating_withoutWriting() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
            val now = Instant.fromEpochMilliseconds(10)
            listOf(-1, 6).forEach { rating ->
                val result = dao.updateRating(isbn, rating, now)
                assertThat(result).isEqualTo(RatingChangeResult.InvalidRating)
                assertThat(dao.getEntry(isbn)).isNull()
            }
        }

    @Test
    fun updateRating_and_changeReadingStatus_preserveEachOther() =
        runTest {
            val isbn = "123"
            val t1 = Instant.fromEpochMilliseconds(10)
            val t2 = Instant.fromEpochMilliseconds(20)
            val t3 = Instant.fromEpochMilliseconds(30)
            bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))

            dao.updateRating(isbn, 4, t1)
            val entryAfterRating = dao.getEntry(isbn)
            assertThat(entryAfterRating?.status).isEqualTo(ReadingStatus.UNSET)
            assertThat(entryAfterRating?.rating).isEqualTo(4)

            dao.changeReadingStatus(isbn, ReadingStatus.READING, t2, null)
            val entryAfterStatus = dao.getEntry(isbn)
            assertThat(entryAfterStatus?.status).isEqualTo(ReadingStatus.READING)
            assertThat(entryAfterStatus?.rating).isEqualTo(4)
            assertThat(entryAfterStatus?.updatedAt).isEqualTo(t2)

            dao.updateRating(isbn, 5, t3)
            val entryFinal = dao.getEntry(isbn)
            assertThat(entryFinal?.status).isEqualTo(ReadingStatus.READING)
            assertThat(entryFinal?.rating).isEqualTo(5)
            assertThat(entryFinal?.updatedAt).isEqualTo(t3)
        }
}
