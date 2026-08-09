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
    fun saveRecord_saves_entity_to_dao() =
        runTest {
            val record =
                ReadingRecord(
                    id = 0L,
                    isbn = "123",
                    date = LocalDate(2024, 4, 12),
                    time = LocalTime(10, 0),
                    startPage = 1,
                    endPage = 50,
                )
            val id = repository.saveRecord(record)

            assertThat(fakeDao.records).hasSize(1)
            assertThat(fakeDao.records[0].isbn).isEqualTo("123")
            assertThat(fakeDao.records[0].endPage).isEqualTo(50)
        }

    @Test
    fun removeRecord_removes_from_dao() =
        runTest {
            fakeDao.records.add(recordEntity(id = 1L, endPage = 50))
            repository.removeRecord(1L)
            assertThat(fakeDao.records).isEmpty()
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

    override suspend fun delete(id: Long) {
        records.removeIf { it.id == id }
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
