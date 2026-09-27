package com.leeseungyun1020.manicule.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.RoomReadingRecordLocalDataSource
import com.leeseungyun1020.manicule.core.database.dao.ReadingRecordDao
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Test

class ReadingRecordRepositoryImplTest {

    private lateinit var repository: ReadingRecordRepositoryImpl
    private lateinit var fakeDao: FakeReadingRecordDao

    @Before
    fun setup() {
        fakeDao = FakeReadingRecordDao()
        repository = ReadingRecordRepositoryImpl(RoomReadingRecordLocalDataSource(fakeDao))
    }

    @Test
    fun addRecord_preservesSessionFieldsAndReturnsGeneratedId() =
        runTest {
            val now = Instant.parse("2026-09-16T10:00:00Z")
            val record = ReadingRecord(0, "123", LocalDate(2026, 9, 15), LocalTime(21, 30), 11, 42)

            val id = repository.addRecord(record, now)

            assertThat(id).isEqualTo(1L)
            assertThat(repository.observeRecordsByIsbn("123").first()).containsExactly(record.copy(id = id))
            assertThat(fakeDao.updatedEntry).isEqualTo("123" to now)
        }

    @Test
    fun saveRecord_updates_existing_record_and_returns_true() =
        runTest {
            fakeDao.records.add(recordEntity(id = 1L, endPage = 50))
            val updated =
                ReadingRecord(
                    id = 1L,
                    isbn = "123",
                    date = LocalDate(2024, 4, 13),
                    time = LocalTime(11, 0),
                    startPage = 1,
                    endPage = 60,
                )
            val result = repository.saveRecord(updated)

            assertThat(result).isTrue()
            assertThat(fakeDao.records).hasSize(1)
            assertThat(fakeDao.records[0].date).isEqualTo(LocalDate(2024, 4, 13))
            assertThat(fakeDao.records[0].time).isEqualTo(LocalTime(11, 0))
            assertThat(fakeDao.records[0].endPage).isEqualTo(60)
        }

    @Test
    fun saveRecord_returns_false_when_id_is_non_positive_or_not_found() =
        runTest {
            fakeDao.records.add(recordEntity(id = 1L, endPage = 50))

            // Non-positive id
            val zeroIdRecord =
                ReadingRecord(
                    id = 0L,
                    isbn = "123",
                    date = LocalDate(2024, 4, 13),
                    time = LocalTime(11, 0),
                    startPage = 1,
                    endPage = 60,
                )
            assertThat(repository.saveRecord(zeroIdRecord)).isFalse()

            // Non-existent id
            val nonExistentRecord =
                ReadingRecord(
                    id = 999L,
                    isbn = "123",
                    date = LocalDate(2024, 4, 13),
                    time = LocalTime(11, 0),
                    startPage = 1,
                    endPage = 60,
                )
            assertThat(repository.saveRecord(nonExistentRecord)).isFalse()

            // Different isbn
            val differentIsbnRecord =
                ReadingRecord(
                    id = 1L,
                    isbn = "456",
                    date = LocalDate(2024, 4, 13),
                    time = LocalTime(11, 0),
                    startPage = 1,
                    endPage = 60,
                )
            assertThat(repository.saveRecord(differentIsbnRecord)).isFalse()
        }

    @Test
    fun removeRecord_removes_from_dao_and_returns_true() =
        runTest {
            fakeDao.records.add(recordEntity(id = 1L, endPage = 50))
            val result = repository.removeRecord(1L, "123")
            assertThat(result).isTrue()
            assertThat(fakeDao.records).isEmpty()
        }

    @Test
    fun removeRecord_returns_false_when_not_found_or_different_isbn_or_invalid() =
        runTest {
            fakeDao.records.add(recordEntity(id = 1L, endPage = 50))

            // Different isbn
            assertThat(repository.removeRecord(1L, "456")).isFalse()
            assertThat(fakeDao.records).isNotEmpty()

            // Non-existent id
            assertThat(repository.removeRecord(999L, "123")).isFalse()

            // Invalid id
            assertThat(repository.removeRecord(0L, "123")).isFalse()
            assertThat(repository.removeRecord(-1L, "123")).isFalse()

            // Blank isbn
            assertThat(repository.removeRecord(1L, "")).isFalse()
        }

    @Test
    fun observeRecordsByIsbn_returns_mapped_flow() =
        runTest {
            fakeDao.records.add(recordEntity(id = 1L, endPage = 50))
            fakeDao.records.add(recordEntity(id = 2L, endPage = 100))

            val records = repository.observeRecordsByIsbn("123").first()
            assertThat(records).hasSize(2)
            assertThat(records[0].isbn).isEqualTo("123")
        }

    @Test
    fun getMaxEndPage_returns_value_from_dao() =
        runTest {
            fakeDao.records.add(recordEntity(id = 1L, endPage = 100))
            fakeDao.records.add(recordEntity(id = 2L, endPage = 50))

            val maxEndPage = repository.getMaxEndPage("123")
            assertThat(maxEndPage).isEqualTo(100)
        }
}

private fun recordEntity(
    id: Long,
    endPage: Int,
) = ReadingRecordEntity(
    id = id,
    isbn = "123",
    date = LocalDate(2024, 4, 12),
    time = LocalTime(10, 0),
    startPage = 1,
    endPage = endPage,
)

class FakeReadingRecordDao : ReadingRecordDao {
    val records = mutableListOf<ReadingRecordEntity>()

    var updatedEntry: Pair<String, Instant>? = null

    override suspend fun registerEntryForNewRecord(
        isbn: String,
        updatedAt: Instant,
    ) = Unit

    override suspend fun insert(record: ReadingRecordEntity): Long = upsert(record)

    override suspend fun updateEntryForNewRecord(
        isbn: String,
        updatedAt: Instant,
    ) {
        updatedEntry = isbn to updatedAt
    }

    override suspend fun upsert(record: ReadingRecordEntity): Long {
        if (record.id == 0L) {
            val newId = (records.maxOfOrNull { it.id } ?: 0L) + 1L
            records.add(record.copy(id = newId))
            return newId
        } else {
            val idx = records.indexOfFirst { it.id == record.id }
            if (idx >= 0) {
                records[idx] = record
            } else {
                records.add(record)
            }
            return record.id
        }
    }

    override suspend fun update(
        id: Long,
        isbn: String,
        date: LocalDate,
        time: LocalTime,
        startPage: Int,
        endPage: Int,
    ): Int {
        val idx = records.indexOfFirst { it.id == id && it.isbn == isbn }
        if (idx >= 0) {
            records[idx] = records[idx].copy(
                date = date,
                time = time,
                startPage = startPage,
                endPage = endPage,
            )
            return 1
        }
        return 0
    }

    override suspend fun delete(id: Long) {
        records.removeIf { it.id == id }
    }

    override suspend fun delete(
        id: Long,
        isbn: String,
    ): Int {
        val removed = records.removeIf { it.id == id && it.isbn == isbn }
        return if (removed) 1 else 0
    }

    override fun observeByIsbn(isbn: String): Flow<List<ReadingRecordEntity>> = flowOf(records.filter { it.isbn == isbn })

    override fun observeBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecordEntity>> =
        flowOf(
            records.filter {
                it.date in start..end
            },
        )

    override suspend fun getMaxEndPage(isbn: String): Int? = records.filter { it.isbn == isbn }.maxOfOrNull(ReadingRecordEntity::endPage)
}
