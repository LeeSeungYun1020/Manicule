package com.leeseungyun1020.manicule.core.domain.stats

import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.StatsRepository
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class ReadingDayBook(
    val isbn: String,
    val book: Book?,
    val recordCount: Int,
    val pagesRead: Int,
)

class GetReadingDayBooksUseCase
    @Inject
    constructor(
        private val statsRepository: StatsRepository,
        private val bookRepository: BookRepository,
    ) {
        operator fun invoke(date: LocalDate): Flow<List<ReadingDayBook>> =
            statsRepository.observeRecordsBetween(date, date).flatMapLatest { records ->
                val groups = records.groupBy { it.isbn }
                    .toList()
                    .sortedWith(
                        compareByDescending<Pair<String, List<ReadingRecord>>> { (_, sessions) ->
                            sessions.maxOf { it.time }
                        }.thenBy { it.first },
                    )
                if (groups.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        groups.map { (isbn, sessions) ->
                            bookRepository.observeBook(isbn).map { book ->
                                ReadingDayBook(
                                    isbn = isbn,
                                    book = book,
                                    recordCount = sessions.size,
                                    pagesRead = sessions.sumOf { it.pagesRead },
                                )
                            }
                        },
                    ) { it.toList() }
                }
            }
    }
