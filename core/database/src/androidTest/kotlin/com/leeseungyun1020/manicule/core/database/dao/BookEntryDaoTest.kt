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
                rating = null,
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
}
