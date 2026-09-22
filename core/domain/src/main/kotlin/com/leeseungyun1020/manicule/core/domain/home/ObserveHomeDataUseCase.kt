package com.leeseungyun1020.manicule.core.domain.home

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.domain.stats.currentStreak
import com.leeseungyun1020.manicule.core.domain.time.observeToday
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import javax.inject.Inject

data class HomeData(
    val hasLibraryBooks: Boolean,
    val hasReadingRecords: Boolean,
    val readingBooks: List<BookEntry>,
    val wantBookCount: Int,
    val summary: HomeReadingSummary?,
)

data class HomeReadingSummary(
    val today: LocalDate,
    val todayPages: Int,
    val currentStreak: Int,
    val recentDays: List<ReadingCalendarDay>,
)

/** 홈이 필요한 기존 서재·통계 흐름을 같은 날짜 기준으로 조합한다. */
class ObserveHomeDataUseCase
    @Inject
    constructor(
        private val getLibraryBooks: GetLibraryBooksUseCase,
        private val statsRepository: StatsRepository,
        private val clock: Clock,
    ) {
        operator fun invoke(summaryRetries: Flow<Unit> = emptyFlow()): Flow<HomeData> =
            clock.observeToday().flatMapLatest { today ->
                val start = today.minus(DatePeriod(days = 6))
                val summary =
                    summaryRetries
                        .onStart { emit(Unit) }
                        .flatMapLatest {
                            combine(
                                statsRepository.observeTotals(today, today),
                                statsRepository.observeDailyReading(start, today),
                            ) { todayTotals, dailyReadings -> todayTotals to dailyReadings }
                                .map { Result.success(it) }
                                .catch { emit(Result.failure(it)) }
                        }
                combine(
                    getLibraryBooks(),
                    getLibraryBooks(ReadingStatus.READING),
                    statsRepository.observeReadingDatesThrough(today),
                    summary,
                ) { allBooks, readingBooks, recordDates, summaryResult ->
                    HomeData(
                        hasLibraryBooks = allBooks.isNotEmpty(),
                        hasReadingRecords = recordDates.isNotEmpty(),
                        readingBooks = readingBooks,
                        wantBookCount = allBooks.count { it.status == ReadingStatus.WANT },
                        summary = summaryResult.getOrNull()?.let { (todayTotals, dailyReadings) ->
                            val pagesByDate = dailyReadings.associate { it.date to it.pagesRead }
                            HomeReadingSummary(
                                today = today,
                                todayPages = todayTotals.pagesRead,
                                currentStreak = currentStreak(recordDates.distinct().sorted(), today),
                                recentDays =
                                    (0..6).map { offset ->
                                        val date = start.plus(DatePeriod(days = offset))
                                        ReadingCalendarDay.of(date, pagesByDate[date] ?: 0)
                                    },
                            )
                        },
                    )
                }
            }
    }
