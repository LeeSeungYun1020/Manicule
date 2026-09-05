package com.leeseungyun1020.manicule.core.domain.library

import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.BookEntry
import com.leeseungyun1020.manicule.core.model.LibrarySort
import com.leeseungyun1020.manicule.core.model.ReadingStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLibraryBooksUseCase
    @Inject
    constructor(
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(): Flow<List<BookEntry>> = libraryRepository.observeAll()

        operator fun invoke(
            status: ReadingStatus,
            sort: LibrarySort = LibrarySort.Default,
        ): Flow<List<BookEntry>> = libraryRepository.observeByStatus(status, sort)
    }
