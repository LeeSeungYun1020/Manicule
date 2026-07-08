package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import javax.inject.Inject

class DeleteBookEntryUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        suspend operator fun invoke(isbn: String) {
            libraryRepository.removeBookEntry(isbn)
        }
    }
