package com.leeseungyun1020.manicule.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.ManiculeDatabase
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test

class ReadingRecordDaoTest {
    private lateinit var db: ManiculeDatabase
    private lateinit var dao: ReadingRecordDao
    private lateinit var bookDao: BookDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ManiculeDatabase::class.java).build()
        dao = db.readingRecordDao()
        bookDao = db.bookDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun upsert_updates_existing_record_by_primary_key() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "T", "A", "P", null, null, null, null, null, null, null, null))

            val id = dao.upsert(record(isbn = isbn, time = LocalTime(9, 0), startPage = 1, endPage = 10))
            dao.upsert(record(id = id, isbn = isbn, time = LocalTime(9, 0), startPage = 1, endPage = 20))

            dao.observeByIsbn(isbn).test {
                val records = awaitItem()
                assertThat(records).hasSize(1)
                assertThat(records.first().endPage).isEqualTo(20)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun same_date_records_are_preserved_and_sorted_by_time_descending() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "T", "A", "P", null, null, null, null, null, null, null, null))
            dao.upsert(record(isbn = isbn, time = LocalTime(9, 0), startPage = 1, endPage = 10))
            dao.upsert(record(isbn = isbn, time = LocalTime(20, 0), startPage = 11, endPage = 20))

            dao.observeByIsbn(isbn).test {
                val records = awaitItem()
                assertThat(records).hasSize(2)
                assertThat(records.map(ReadingRecordEntity::time))
                    .containsExactly(LocalTime(20, 0), LocalTime(9, 0))
                    .inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observeBetween_sorts_by_date_and_time_descending() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "T", "A", "P", null, null, null, null, null, null, null, null))
            dao.upsert(record(isbn = isbn, date = LocalDate(2024, 1, 1), time = LocalTime(20, 0)))
            dao.upsert(record(isbn = isbn, date = LocalDate(2024, 1, 2), time = LocalTime(9, 0)))
            dao.upsert(record(isbn = isbn, date = LocalDate(2024, 1, 2), time = LocalTime(20, 0)))

            dao.observeBetween(LocalDate(2024, 1, 1), LocalDate(2024, 1, 2)).test {
                val records = awaitItem()
                assertThat(records.map { it.date to it.time })
                    .containsExactly(
                        LocalDate(2024, 1, 2) to LocalTime(20, 0),
                        LocalDate(2024, 1, 2) to LocalTime(9, 0),
                        LocalDate(2024, 1, 1) to LocalTime(20, 0),
                    ).inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun getMaxEndPage_returns_highest_end_page() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "T", "A", "P", null, null, null, null, null, null, null, null))
            dao.upsert(record(isbn = isbn, startPage = 80, endPage = 100))
            dao.upsert(record(isbn = isbn, time = LocalTime(11, 0), startPage = 1, endPage = 50))

            assertThat(dao.getMaxEndPage(isbn)).isEqualTo(100)
        }

    private fun record(
        id: Long = 0,
        isbn: String,
        date: LocalDate = LocalDate(2024, 1, 1),
        time: LocalTime = LocalTime(10, 0),
        startPage: Int = 1,
        endPage: Int = 10,
    ) = ReadingRecordEntity(
        id = id,
        isbn = isbn,
        date = date,
        time = time,
        startPage = startPage,
        endPage = endPage,
    )
}
