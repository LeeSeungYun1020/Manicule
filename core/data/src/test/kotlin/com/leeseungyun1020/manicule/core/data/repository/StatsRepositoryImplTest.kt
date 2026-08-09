package com.leeseungyun1020.manicule.core.data.repository

import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.data.datasource.ReadingStatsLocalDataSource
import com.leeseungyun1020.manicule.core.database.dao.projection.DailyReadingProjection
import com.leeseungyun1020.manicule.core.database.dao.projection.ReadingTotalsProjection
import com.leeseungyun1020.manicule.core.database.entity.ReadingRecordEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.junit.Before
import org.junit.Test

class StatsRepositoryImplTest {
    private lateinit var localDataSource: FakeReadingStatsLocalDataSource
    private lateinit var repository: StatsRepositoryImpl

    @Before
    fun setUp() {
        localDataSource = FakeReadingStatsLocalDataSource()
        repository = StatsRepositoryImpl(localDataSource)
    }

    @Test
    fun records_are_mapped_without_changing_session_pages() =
        runTest {
            localDataSource.records.value =
                listOf(
                    record(startPage = 1, endPage = 10),
                    record(id = 2, startPage = 5, endPage = 15),
                )

            val records = repository.observeRecordsBetween(DATE, DATE).first()

            assertThat(records.map { it.pagesRead }).containsExactly(10, 11).inOrder()
        }

    @Test
    fun daily_reading_and_totals_are_mapped_and_reemitted() =
        runTest {
            localDataSource.daily.value = listOf(DailyReadingProjection(DATE, 21, 2))
            localDataSource.totals.value = ReadingTotalsProjection(21, 2)

            assertThat(repository.observeDailyReading(DATE, DATE).first().single().pagesRead).isEqualTo(21)
            assertThat(repository.observeTotals(DATE, DATE).first().bookCount).isEqualTo(2)

            localDataSource.daily.value = listOf(DailyReadingProjection(DATE, 32, 2))
            assertThat(repository.observeDailyReading(DATE, DATE).first().single().pagesRead).isEqualTo(32)
        }

    @Test
    fun reading_dates_are_forwarded_in_order() =
        runTest {
            localDataSource.dates.value = listOf(LocalDate(2024, 2, 28), DATE)

            assertThat(repository.observeReadingDatesThrough(DATE).first())
                .containsExactly(LocalDate(2024, 2, 28), DATE)
                .inOrder()
        }

    @Test
    fun reversed_ranges_are_rejected_by_all_range_queries() {
        val start = LocalDate(2024, 3, 2)
        val end = LocalDate(2024, 3, 1)

        listOf(
            { repository.observeRecordsBetween(start, end) },
            { repository.observeDailyReading(start, end) },
            { repository.observeTotals(start, end) },
        ).forEach { query ->
            assertThat(runCatching(query).exceptionOrNull()).isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    private fun record(
        id: Long = 1,
        startPage: Int,
        endPage: Int,
    ) = ReadingRecordEntity(
        id = id,
        isbn = "isbn-$id",
        date = DATE,
        time = LocalTime(10, 0),
        startPage = startPage,
        endPage = endPage,
    )

    private companion object {
        val DATE = LocalDate(2024, 2, 29)
    }
}

private class FakeReadingStatsLocalDataSource : ReadingStatsLocalDataSource {
    val records = MutableStateFlow<List<ReadingRecordEntity>>(emptyList())
    val daily = MutableStateFlow<List<DailyReadingProjection>>(emptyList())
    val totals = MutableStateFlow(ReadingTotalsProjection(0, 0))
    val dates = MutableStateFlow<List<LocalDate>>(emptyList())

    override fun observeRecordsBetween(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<ReadingRecordEntity>> = records

    override fun observeDailyReading(
        start: LocalDate,
        end: LocalDate,
    ): Flow<List<DailyReadingProjection>> = daily

    override fun observeTotals(
        start: LocalDate,
        end: LocalDate,
    ): Flow<ReadingTotalsProjection> = totals

    override fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>> = dates
}
