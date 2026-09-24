package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.data.repository.SaveBookEntryResult
import com.leeseungyun1020.manicule.core.model.BookEntry
import javax.inject.Inject

/** Restores the complete entry snapshot, including its original sorting timestamps. */
class RestoreBookEntryUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(entry: BookEntry): Boolean = libraryRepository.saveBookEntry(entry) == SaveBookEntryResult.Saved
    }
