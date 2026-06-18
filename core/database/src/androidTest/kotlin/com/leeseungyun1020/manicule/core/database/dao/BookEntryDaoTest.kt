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
    fun currentPage_is_from_latest_date_even_if_cumulative_is_lower() =
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

            recordDao.upsert(ReadingRecordEntity(isbn = isbn, date = LocalDate(2024, 1, 1), cumulativePage = 100))
            recordDao.upsert(ReadingRecordEntity(isbn = isbn, date = LocalDate(2024, 1, 2), cumulativePage = 50))

            dao.observeByIsbn(isbn).test {
                val result = awaitItem()
                assertThat(result?.currentPage).isEqualTo(50)
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
}
