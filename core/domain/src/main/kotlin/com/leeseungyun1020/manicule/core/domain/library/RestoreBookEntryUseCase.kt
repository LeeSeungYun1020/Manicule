package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.datetime.Instant
import javax.inject.Inject

/** Undoes a library action without replacing newer entry or book data. */
class RestoreBookEntryUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend fun deletedEntry(entry: BookEntry): Boolean = libraryRepository.restoreDeletedEntryIfAbsent(entry)

        suspend fun readingStatus(
            original: BookEntry,
            changedStatus: ReadingStatus,
            changedAt: Instant,
        ): Boolean = libraryRepository.restoreReadingStatusIfUnchanged(original, changedStatus, changedAt)
    }
