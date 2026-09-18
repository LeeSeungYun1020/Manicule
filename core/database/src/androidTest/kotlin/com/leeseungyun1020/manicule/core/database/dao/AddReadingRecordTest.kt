package com.leeseungyun1020.manicule.core.database.dao

import android.content.Context
import android.database.sqlite.SQLiteException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.database.ManiculeDatabase
import com.leeseungyun1020.manicule.core.database.entity.BookEntity
import com.leeseungyun1020.manicule.core.database.entity.BookEntryEntity
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.After
import org.junit.Before
import org.junit.Test

class AddReadingRecordTest {
    private lateinit var db: ManiculeDatabase
    private lateinit var dao: ReadingRecordDao
    private val before = Instant.parse("2026-09-01T10:00:00Z")
    private val now = Instant.parse("2026-09-16T10:00:00Z")
    private val date = LocalDate(2026, 9, 15)

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(), ManiculeDatabase::class.java).build()
        dao = db.readingRecordDao()
    }

    @After
    fun closeDb() = db.close()

    @Test
    fun firstWantRecordTransitionsAndPreservesReview() =
        runTest {
            val entry = seed(ReadingStatus.WANT)
            seedBook("other")
            dao.upsert(record("other"))
            val id = dao.add(record(), now)

            assertThat(id).isGreaterThan(0L)
            assertThat(dao.observeByIsbn("123").first()).containsExactly(record().copy(id = id))
            assertThat(db.bookEntryDao().getEntry("123")).isEqualTo(entry.copy(status = ReadingStatus.READING, updatedAt = now))
        }

    @Test
    fun existingWantRecordDoesNotTransitionAndProgressUsesMaximum() =
        runTest {
            val entry = seed(ReadingStatus.WANT)
            dao.upsert(record().copy(startPage = 43, endPage = 68))
            dao.add(record(), now)

            assertThat(db.bookEntryDao().getEntry("123")).isEqualTo(entry.copy(updatedAt = now))
            assertThat(dao.getMaxEndPage("123")).isEqualTo(68)
            assertThat(dao.observeByIsbn("123").first()).hasSize(2)
        }

    @Test
    fun otherStatusesAndFinishedDateArePreserved() =
        runTest {
            listOf(ReadingStatus.READING, ReadingStatus.FINISHED).forEach { status ->
                val isbn = status.name
                val entry = seed(status, isbn)
                dao.add(record(isbn), now)
                assertThat(db.bookEntryDao().getEntry(isbn)).isEqualTo(entry.copy(updatedAt = now))
            }
        }

    @Test
    fun unregisteredBookIsRegisteredAsReading() =
        runTest {
            seedBook("123")
            dao.add(record(), now)
            assertThat(db.bookEntryDao().getEntry("123"))
                .isEqualTo(BookEntryEntity("123", ReadingStatus.READING, 0, null, now, now, null))
            assertThat(dao.observeByIsbn("123").first()).hasSize(1)
        }

    @Test
    fun unsetTransitionsWithOrWithoutPreviousRecordsAndPreservesReview() =
        runTest {
            listOf(false, true).forEach { hasRecords ->
                val isbn = "unset-$hasRecords"
                val entry = seed(ReadingStatus.UNSET, isbn)
                if (hasRecords) dao.upsert(record(isbn))

                dao.add(record(isbn), now)

                assertThat(db.bookEntryDao().getEntry(isbn)).isEqualTo(entry.copy(status = ReadingStatus.READING, updatedAt = now))
                assertThat(dao.observeByIsbn(isbn).first()).hasSize(if (hasRecords) 2 else 1)
            }
        }

    @Test
    fun insertFailureRollsBackNewLibraryEntry() =
        runTest {
            seedBook("123")
            db.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER fail_record BEFORE INSERT ON reading_records BEGIN SELECT RAISE(ABORT, 'write failure'); END",
            )
            try {
                dao.add(record(), now)
                error("Expected insert failure")
            } catch (_: SQLiteException) {
                assertThat(db.bookEntryDao().getEntry("123")).isNull()
                assertThat(dao.observeByIsbn("123").first()).isEmpty()
            }
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_record")
            dao.add(record(), now)
            assertThat(db.bookEntryDao().getEntry("123")?.status).isEqualTo(ReadingStatus.READING)
            assertThat(dao.observeByIsbn("123").first()).hasSize(1)
        }

    @Test
    fun missingBookFailsWithoutSavingRecord() =
        runTest {
            try {
                dao.add(record(), now)
                error("Expected foreign key failure")
            } catch (_: SQLiteException) {
                assertThat(dao.observeByIsbn("123").first()).isEmpty()
            }
        }

    @Test
    fun insertFailureRollsBackStatusAndModificationTime() =
        runTest {
            val entry = seed(ReadingStatus.WANT)
            db.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER fail_record BEFORE INSERT ON reading_records BEGIN SELECT RAISE(ABORT, 'write failure'); END",
            )
            try {
                dao.add(record(), now)
                error("Expected insert failure")
            } catch (_: SQLiteException) {
                assertThat(db.bookEntryDao().getEntry("123")).isEqualTo(entry)
                assertThat(dao.observeByIsbn("123").first()).isEmpty()
            }
            db.openHelper.writableDatabase.execSQL("DROP TRIGGER fail_record")
            dao.add(record(), now)
            assertThat(dao.observeByIsbn("123").first()).hasSize(1)
            assertThat(db.bookEntryDao().getEntry("123")).isEqualTo(entry.copy(status = ReadingStatus.READING, updatedAt = now))
        }

    @Test
    fun invalidOrExistingRecordIsRejectedBeforeMutation() =
        runTest {
            val entry = seed(ReadingStatus.WANT)
            val id = dao.upsert(record())
            listOf(record().copy(id = id), record().copy(startPage = 0), record().copy(endPage = 0)).forEach { invalid ->
                try {
                    dao.add(invalid, now)
                    error("Expected invalid record")
                } catch (_: IllegalArgumentException) {
                    assertThat(db.bookEntryDao().getEntry("123")).isEqualTo(entry)
                    assertThat(dao.observeByIsbn("123").first()).containsExactly(record().copy(id = id))
                }
            }
        }

    @Test
    fun concurrentSessionsGetDistinctIdsAndPreserveBothRecords() =
        runTest {
            val entry = seed(ReadingStatus.WANT)
            val ids = listOf(async { dao.add(record(), now) }, async { dao.add(record(), now) }).awaitAll()

            assertThat(ids.distinct()).hasSize(2)
            assertThat(dao.observeByIsbn("123").first()).hasSize(2)
            assertThat(db.bookEntryDao().getEntry("123")).isEqualTo(entry.copy(status = ReadingStatus.READING, updatedAt = now))
        }

    private suspend fun seed(
        status: ReadingStatus,
        isbn: String = "123",
    ): BookEntryEntity {
        seedBook(isbn)
        val entry = BookEntryEntity(isbn, status, 4, "memo", before, before, if (status == ReadingStatus.FINISHED) date else null)
        db.bookEntryDao().upsert(entry)
        return entry
    }

    private suspend fun seedBook(isbn: String) {
        db.bookDao().upsert(BookEntity(isbn, "Title", "Author", "Publisher", null, null, 100, null, null, null, null, null))
    }

    private fun record(isbn: String = "123") =
        ReadingRecordEntity(
            isbn = isbn,
            date = date,
            time = LocalTime(21, 30),
            startPage = 1,
            endPage = 10,
        )
}
