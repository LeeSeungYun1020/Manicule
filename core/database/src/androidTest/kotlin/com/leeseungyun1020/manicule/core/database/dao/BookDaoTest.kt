package com.leeseungyun1020.manicule.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.ManiculeDatabase
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import kotlinx.coroutines.test.runTest
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
}
