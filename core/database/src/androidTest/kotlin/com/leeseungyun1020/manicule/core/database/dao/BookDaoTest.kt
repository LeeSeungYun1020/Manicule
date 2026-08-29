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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test

class BookDaoTest {
    private lateinit var db: ManiculeDatabase
    private lateinit var dao: BookDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, ManiculeDatabase::class.java).build()
        dao = db.bookDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun upsert_and_getByIsbn() =
        runTest {
            val book = BookEntity("123", "Title", "Author", "Pub", null, null, null, null, null, null, null, null)
            dao.upsert(book)

            val retrieved = dao.getByIsbn("123")
            assertThat(retrieved).isEqualTo(book)
        }

    @Test
    fun observeByIsbn() =
        runTest {
            val book = BookEntity("123", "Title", "Author", "Pub", null, null, null, null, null, null, null, null)

            dao.observeByIsbn("123").test {
                assertThat(awaitItem()).isNull()

                dao.upsert(book)
                assertThat(awaitItem()).isEqualTo(book)
            }
        }

    @Test
    fun upsert_existingBook_preservesEntryAndReadingRecord() =
        runTest {
            val isbn = "123"
            val book = BookEntity(isbn, "Title", "Author", "Pub", null, null, null, null, null, null, null, null)
            val entry =
                BookEntryEntity(
                    isbn = isbn,
                    status = ReadingStatus.READING,
                    rating = 0,
                    memo = null,
                    addedAt = Instant.fromEpochMilliseconds(1),
                    updatedAt = Instant.fromEpochMilliseconds(1),
                    finishedAt = null,
                )
            val record =
                ReadingRecordEntity(
                    id = 1,
                    isbn = isbn,
                    date = LocalDate(2026, 8, 27),
                    time = LocalTime(12, 0),
                    startPage = 1,
                    endPage = 10,
                )
            dao.upsert(book)
            db.bookEntryDao().upsert(entry)
            db.readingRecordDao().upsert(record)

            dao.upsert(book.copy(title = "Updated"))

            assertThat(db.bookEntryDao().observeByIsbn(isbn).first()).isNotNull()
            assertThat(db.readingRecordDao().observeByIsbn(isbn).first()).containsExactly(record)
        }
}
