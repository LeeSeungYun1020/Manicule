package com.leeseungyun1020.manicule.core.domain.record

import com.leeseungyun1020.manicule.core.common.time.Clock
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.ReadingRecordRepository
import com.leeseungyun1020.manicule.core.model.ReadingRecord
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import javax.inject.Inject

class AddReadingRecordUseCase
    @Inject
    constructor(
        private val readingRecordRepository: ReadingRecordRepository,
        private val bookRepository: BookRepository,
        private val libraryRepository: LibraryRepository,
        private val clock: Clock,
    ) {
        /** 미등록 책은 READING으로 등록하고 UNSET·첫 WANT 기록은 READING으로 전환한다. */
        suspend operator fun invoke(
            isbn: String,
            date: LocalDate,
            time: LocalTime,
            startPage: Int,
            endPage: Int,
        ): AddRecordResult {
            require(isbn.isNotBlank()) { "isbn must not be blank" }
            require(startPage >= 1) { "startPage must be >= 1" }
            require(endPage >= startPage) { "endPage must be >= startPage" }
            val record = ReadingRecord(0, isbn, date, time, startPage, endPage)
            val recordId = readingRecordRepository.addRecord(record, clock.now())
            val totalPages = bookRepository.observeBook(isbn).first()?.totalPages
            val maxEndPage = readingRecordRepository.getMaxEndPage(isbn) ?: 0
            val currentStatus = libraryRepository.observeBookEntry(isbn).first()?.status
            return AddRecordResult(
                recordId = recordId,
                shouldCheckFinish = shouldCheckFinish(totalPages, maxEndPage, currentStatus),
                maxEndPage = maxEndPage,
            )
        }

        private fun shouldCheckFinish(
            totalPages: Int?,
            maxEndPage: Int,
            currentStatus: ReadingStatus?,
        ): Boolean {
            if (totalPages == null || totalPages <= 0) return false
            if (currentStatus == ReadingStatus.FINISHED) return false
            val remaining = totalPages - maxEndPage
            return remaining <= (totalPages * 0.1).toInt() || remaining <= 40
        }
    }
