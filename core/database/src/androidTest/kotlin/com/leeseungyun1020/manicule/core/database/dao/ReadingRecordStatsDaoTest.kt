package com.leeseungyun1020.manicule.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReadingRecordStatsDaoTest {
    private lateinit var database: ManiculeDatabase
    private lateinit var statsDao: ReadingRecordStatsDao
    private lateinit var recordDao: ReadingRecordDao
    private lateinit var bookDao: BookDao

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, ManiculeDatabase::class.java).build()
        statsDao = database.readingRecordStatsDao()
        recordDao = database.readingRecordDao()
        bookDao = database.bookDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun records_include_both_boundaries_and_sort_ascending() =
        runTest {
            saveBooks("a")
            recordDao.upsert(record("a", LocalDate(2024, 1, 3), LocalTime(12, 0)))
            recordDao.upsert(record("a", LocalDate(2024, 1, 1), LocalTime(12, 0)))
            recordDao.upsert(record("a", LocalDate(2024, 1, 2), LocalTime(20, 0)))
            recordDao.upsert(record("a", LocalDate(2024, 1, 2), LocalTime(9, 0)))

            statsDao.observeRecordsBetween(LocalDate(2024, 1, 1), LocalDate(2024, 1, 2)).test {
                assertThat(awaitItem().map { it.date to it.time })
                    .containsExactly(
                        LocalDate(2024, 1, 1) to LocalTime(12, 0),
                        LocalDate(2024, 1, 2) to LocalTime(9, 0),
                        LocalDate(2024, 1, 2) to LocalTime(20, 0),
                    ).inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun daily_and_period_aggregates_sum_sessions_and_count_unique_books() =
        runTest {
            saveBooks("a", "b")
            val firstDay = LocalDate(2024, 2, 28)
            val secondDay = LocalDate(2024, 2, 29)
            recordDao.upsert(record("a", firstDay, startPage = 1, endPage = 10))
            recordDao.upsert(record("a", firstDay, time = LocalTime(11, 0), startPage = 5, endPage = 15))
            recordDao.upsert(record("b", firstDay, time = LocalTime(12, 0), startPage = 20, endPage = 24))
            recordDao.upsert(record("a", secondDay, startPage = 16, endPage = 20))

            statsDao.observeDailyReading(firstDay, secondDay).test {
                val daily = awaitItem()
                assertThat(daily.map { it.pagesRead }).containsExactly(26, 5).inOrder()
                assertThat(daily.map { it.bookCount }).containsExactly(2, 1).inOrder()
                cancelAndIgnoreRemainingEvents()
            }
            statsDao.observeTotals(firstDay, secondDay).test {
                val totals = awaitItem()
                assertThat(totals.pagesRead).isEqualTo(31)
                assertThat(totals.bookCount).isEqualTo(2)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun empty_aggregates_are_zero_and_flow_reemits_after_insert() =
        runTest {
            saveBooks("a")
            val date = LocalDate(2024, 3, 1)

            statsDao.observeTotals(date, date).test {
                assertThat(awaitItem().pagesRead).isEqualTo(0)

                recordDao.upsert(record("a", date, startPage = 11, endPage = 42))

                val updated = awaitItem()
                assertThat(updated.pagesRead).isEqualTo(32)
                assertThat(updated.bookCount).isEqualTo(1)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun reading_dates_are_distinct_sorted_and_exclude_after_cutoff() =
        runTest {
            saveBooks("a", "b")
            recordDao.upsert(record("a", LocalDate(2024, 2, 29)))
            recordDao.upsert(record("b", LocalDate(2024, 2, 29)))
            recordDao.upsert(record("a", LocalDate(2024, 2, 28)))
            recordDao.upsert(record("a", LocalDate(2024, 3, 1)))

            statsDao.observeReadingDatesThrough(LocalDate(2024, 2, 29)).test {
                assertThat(awaitItem())
                    .containsExactly(LocalDate(2024, 2, 28), LocalDate(2024, 2, 29))
                    .inOrder()
                cancelAndIgnoreRemainingEvents()
            }
        }

    private suspend fun saveBooks(vararg isbns: String) {
        isbns.forEach { isbn ->
            bookDao.upsert(
                BookEntity(
                    isbn = isbn,
                    title = isbn,
                    author = "author",
                    publisher = "publisher",
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
        }
    }

    private fun record(
        isbn: String,
        date: LocalDate,
        time: LocalTime = LocalTime(10, 0),
        startPage: Int = 1,
        endPage: Int = 10,
    ) = ReadingRecordEntity(
        isbn = isbn,
        date = date,
        time = time,
        startPage = startPage,
        endPage = endPage,
    )
}
