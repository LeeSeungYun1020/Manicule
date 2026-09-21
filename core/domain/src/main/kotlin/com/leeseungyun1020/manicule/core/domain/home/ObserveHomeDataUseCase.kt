package com.leeseungyun1020.manicule.core.domain.home

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.domain.library.GetLibraryBooksUseCase
import com.leeseungyun1020.manicule.core.domain.stats.currentStreak
import com.leeseungyun1020.manicule.core.domain.stats.observeToday
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingCalendarDay
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
        operator fun invoke(): Flow<HomeData> =
            clock.observeToday().flatMapLatest { today ->
                val start = today.minus(DatePeriod(days = 6))
                combine(
                    getLibraryBooks(),
                    getLibraryBooks(ReadingStatus.READING),
                    statsRepository.observeReadingDatesThrough(today),
                    statsRepository.observeTotals(today, today),
                    statsRepository.observeDailyReading(start, today),
                ) { allBooks, readingBooks, recordDates, todayTotals, dailyReadings ->
                    val pagesByDate = dailyReadings.associate { it.date to it.pagesRead }
                    HomeData(
                        hasLibraryBooks = allBooks.isNotEmpty(),
                        hasReadingRecords = recordDates.isNotEmpty(),
                        readingBooks = readingBooks,
                        wantBookCount = allBooks.count { it.status == ReadingStatus.WANT },
                        today = today,
                        todayPages = todayTotals.pagesRead,
                        currentStreak = currentStreak(recordDates.distinct().sorted(), today),
                        recentDays =
                            (0..6).map { offset ->
                                val date = start.plus(DatePeriod(days = offset))
                                ReadingCalendarDay.of(date, pagesByDate[date] ?: 0)
                            },
                    )
                }
            }
    }
