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
                    null,
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
                    null,
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
    fun observeAll_ordersByUpdatedAtDescendingThenIsbnAscending_andReemits() =
        runTest {
            insertEntry("9783", ReadingStatus.WANT, updatedAt = 10)
            insertEntry("9782", ReadingStatus.READING, updatedAt = 20)
            insertEntry("9781", ReadingStatus.FINISHED, updatedAt = 20)

            dao.observeAll().test {
                assertThat(awaitItem().map { it.entry.isbn }).containsExactly("9781", "9782", "9783").inOrder()

                dao.upsert(entry("9783", ReadingStatus.WANT, updatedAt = 30))
                assertThat(awaitItem().map { it.entry.isbn }).containsExactly("9783", "9781", "9782").inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observeByStatus_filtersAndUsesDeterministicOrder() =
        runTest {
            insertEntry("9783", ReadingStatus.READING, updatedAt = 10)
            insertEntry("9782", ReadingStatus.WANT, updatedAt = 20)
            insertEntry("9781", ReadingStatus.WANT, updatedAt = 20)

            dao.observeByStatus(ReadingStatus.WANT).test {
                assertThat(awaitItem().map { it.entry.isbn }).containsExactly("9781", "9782").inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }

    private suspend fun insertEntry(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Long,
    ) {
        bookDao.upsert(BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null))
        dao.upsert(entry(isbn, status, updatedAt))
    }

    private fun entry(
        isbn: String,
        status: ReadingStatus,
        updatedAt: Long,
    ) = BookEntryEntity(
        isbn = isbn,
        status = status,
        rating = null,
        memo = null,
        addedAt = Instant.fromEpochMilliseconds(0),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt),
        finishedAt = null,
    )
}
