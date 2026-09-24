package com.leeseungyun1020.manicule.feature.stats

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.google.common.truth.Truth.assertThat
import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.BookSyncResult
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.domain.stats.GetPeriodSummaryUseCase
import com.leeseungyun1020.manicule.core.domain.stats.GetReadingCalendarUseCase
import com.leeseungyun1020.manicule.core.domain.stats.GetReadingDayBooksUseCase
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.DailyReading
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingTotals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], manifest = Config.NONE)
class StatsViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private val today = LocalDate(2024, 3, 1)
    private val repository = FakeRepository()
    private val clock = object : Clock {
        override fun now(): Instant = Instant.parse("2024-03-01T12:00:00Z")

        override fun timeZone(): TimeZone = TimeZone.UTC
    }

    @Test
    fun calendar_uses_28_inclusive_days_and_period_summary() =
        runTest(dispatcherRule.dispatcher) {
            repository.records.value = listOf(record(1, LocalDate(2024, 2, 29), "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            val content = viewModel.uiState.value.period as PeriodState.Content
            assertThat(content.days).hasSize(28)
            assertThat(content.days.first().date).isEqualTo(LocalDate(2024, 2, 3))
            assertThat(content.days.last().date).isEqualTo(today)
            assertThat(content.summary.pagesRead).isEqualTo(10)
            assertThat(content.summary.longestStreak).isEqualTo(1)
            job.cancel()
        }

    @Test
    fun only_read_dates_open_and_removing_last_record_keeps_empty_sheet() =
        runTest(dispatcherRule.dispatcher) {
            val date = LocalDate(2024, 2, 29)
            repository.records.value = listOf(record(1, date, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            viewModel.selectDate(today)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)

            viewModel.selectDate(date)
            runCurrent()
            val selected = viewModel.uiState.value.day as DayState.Content
            assertThat(selected.rows.single().pagesRead).isEqualTo(10)

            repository.records.value = emptyList()
            runCurrent()
            assertThat((viewModel.uiState.value.day as DayState.Content).rows).isEmpty()
            viewModel.dismissDay()
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)
            job.cancel()
        }

    @Test
    fun restored_out_of_range_date_is_cleared() =
        runTest(dispatcherRule.dispatcher) {
            val handle = SavedStateHandle(mapOf("selected_date" to "2024-01-01"))
            val viewModel = viewModel(handle)
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            assertThat(handle.get<String>("selected_date")).isNull()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Closed)
            job.cancel()
        }

    @Test
    fun initial_period_error_recovers_on_retry() =
        runTest(dispatcherRule.dispatcher) {
            repository.failPeriod = true
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()
            assertThat(viewModel.uiState.value.period).isEqualTo(PeriodState.Error)

            repository.failPeriod = false
            viewModel.retryPeriod()
            runCurrent()
            assertThat(viewModel.uiState.value.period).isInstanceOf(PeriodState.Content::class.java)
            job.cancel()
        }

    @Test
    fun day_error_retries_without_losing_calendar() =
        runTest(dispatcherRule.dispatcher) {
            val date = LocalDate(2024, 2, 29)
            repository.records.value = listOf(record(1, date, "a", 1, 10))
            val viewModel = viewModel()
            val job = backgroundScope.launch { viewModel.uiState.collect {} }
            runCurrent()

            repository.failDay = true
            viewModel.selectDate(date)
            runCurrent()
            assertThat(viewModel.uiState.value.day).isEqualTo(DayState.Error(date))
            assertThat(viewModel.uiState.value.period).isInstanceOf(PeriodState.Content::class.java)

            repository.failDay = false
            viewModel.retryDay()
            runCurrent()
            assertThat((viewModel.uiState.value.day as DayState.Content).rows.single().pagesRead).isEqualTo(10)
            job.cancel()
        }

    private fun viewModel(handle: SavedStateHandle = SavedStateHandle()) =
        StatsViewModel(
            GetReadingCalendarUseCase(repository),
            GetPeriodSummaryUseCase(repository),
            GetReadingDayBooksUseCase(repository, FakeBooks()),
            clock,
            handle,
        )

    private fun record(
        id: Long,
        date: LocalDate,
        isbn: String,
        start: Int,
        end: Int,
    ) = ReadingRecord(id, isbn, date, LocalTime(12, 0), start, end)

    private class FakeRepository : StatsRepository {
        val records = MutableStateFlow<List<ReadingRecord>>(emptyList())
        var failPeriod = false
        var failDay = false

        override fun observeRecordsBetween(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<ReadingRecord>> =
            records.map { list ->
                if (start == end && failDay) error("day failed")
                if (start != end && failPeriod) error("period failed")
                list.filter { it.date in start..end }
            }

        override fun observeDailyReading(
            start: LocalDate,
            end: LocalDate,
        ): Flow<List<DailyReading>> =
            records.map { list ->
                list.filter { it.date in start..end }.groupBy { it.date }
                    .map { (date, sessions) ->
                        DailyReading(date, sessions.sumOf { it.pagesRead }, sessions.distinctBy { it.isbn }.size)
                    }
            }

        override fun observeTotals(
            start: LocalDate,
            end: LocalDate,
        ): Flow<ReadingTotals> = flowOf(ReadingTotals(0, 0))

        override fun observeReadingDatesThrough(end: LocalDate): Flow<List<LocalDate>> = flowOf(emptyList())
    }

    private class FakeBooks : BookRepository {
        override fun observeBook(isbn: String): Flow<Book?> = flowOf(null)

        override suspend fun syncBook(isbn: String): Result<BookSyncResult> = error("Unexpected sync")

        override fun searchBooks(query: String): Flow<PagingData<Book>> = flowOf(PagingData.empty())
    }
}
