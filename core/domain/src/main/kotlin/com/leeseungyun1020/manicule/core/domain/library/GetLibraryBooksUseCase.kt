package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLibraryBooksUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(status: ReadingStatus? = null): Flow<List<BookEntry>> =
            status?.let(libraryRepository::observeByStatus) ?: libraryRepository.observeAll()
    }
