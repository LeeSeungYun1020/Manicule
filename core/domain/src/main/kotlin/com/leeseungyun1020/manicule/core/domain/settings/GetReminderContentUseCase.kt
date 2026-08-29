package com.leeseungyun1020.manicule.core.domain.settings

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import javax.inject.Inject

class GetReminderContentUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(): List<ReminderContent.Book> =
            libraryRepository
                .getRecentBooksByStatus(ReadingStatus.READING, RECENT_BOOK_LIMIT)
                .mapNotNull { book ->
                    book.title.takeIf(String::isNotBlank)?.let(ReminderContent::Book)
                }

        private companion object {
            const val RECENT_BOOK_LIMIT = 5
        }
    }
