package com.leeseungyun1020.manicule.core.domain.settings

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetReminderContentUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(): ReminderContent {
            val title =
                libraryRepository
                    .observeByStatus(ReadingStatus.READING)
                    .first()
                    .maxByOrNull { it.updatedAt }
                    ?.book
                    ?.title
                    ?.takeIf(String::isNotBlank)

            return title?.let(ReminderContent::Book) ?: ReminderContent.Generic
        }
    }
