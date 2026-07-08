package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.BookEntry
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBookEntryUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(isbn: String): Flow<BookEntry?> = libraryRepository.observeBookEntry(isbn)
    }
