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
    fun upsert_updates_existing_record() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "T", "A", "P", null, null, null, null, null, null, null, null))

            dao.upsert(ReadingRecordEntity(isbn = isbn, date = LocalDate(2024, 1, 1), cumulativePage = 10))
            dao.upsert(ReadingRecordEntity(isbn = isbn, date = LocalDate(2024, 1, 1), cumulativePage = 20))

            dao.observeByIsbn(isbn).test {
                val records = awaitItem()
                assertThat(records).hasSize(1)
                assertThat(records.first().cumulativePage).isEqualTo(20)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun observeBetween() =
        runTest {
            val isbn = "123"
            bookDao.upsert(BookEntity(isbn, "T", "A", "P", null, null, null, null, null, null, null, null))
            dao.upsert(ReadingRecordEntity(isbn = isbn, date = LocalDate(2024, 1, 1), cumulativePage = 10))
            dao.upsert(ReadingRecordEntity(isbn = isbn, date = LocalDate(2024, 1, 2), cumulativePage = 20))
            dao.upsert(ReadingRecordEntity(isbn = isbn, date = LocalDate(2024, 1, 3), cumulativePage = 30))

            dao.observeBetween(LocalDate(2024, 1, 1), LocalDate(2024, 1, 2)).test {
                val records = awaitItem()
                assertThat(records).hasSize(2)
                assertThat(records[0].cumulativePage).isEqualTo(10)
                assertThat(records[1].cumulativePage).isEqualTo(20)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
