package com.leeseungyun1020.manicule.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.ManiculeDatabase
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import com.leeseungyun1020.manicule.core.model.ReadingStatusChangeResult
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
                rating = 0,
                memo = null,
                addedAt = updatedAt,
                updatedAt = updatedAt,
                finishedAt = null,
            ),
        )
    }

    @Test
    fun observeByStatus_filtersAndUsesDeterministicOrder() =
        runTest {
            saveBookEntry("9783", ReadingStatus.READING, Instant.fromEpochMilliseconds(10))
            saveBookEntry("9782", ReadingStatus.WANT, Instant.fromEpochMilliseconds(20))
            saveBookEntry("9781", ReadingStatus.WANT, Instant.fromEpochMilliseconds(20))

            dao.observeByStatus(ReadingStatus.WANT).test {
                assertThat(awaitItem().map { it.entry.isbn }).containsExactly("9781", "9782").inOrder()
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
                dao.observeByStatus(status).test {
                    assertThat(awaitItem().map { it.entry.isbn }).contains(isbn)
                    cancelAndIgnoreRemainingEvents()
                }
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
}
