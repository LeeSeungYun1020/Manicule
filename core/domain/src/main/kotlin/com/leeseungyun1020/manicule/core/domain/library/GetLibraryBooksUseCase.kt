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
        /**
         * 서재 도서 목록 조회. status null이면 전체.
         * TODO: 3단계 Slice 3에서 정렬 로직 추가
         */
        operator fun invoke(status: ReadingStatus? = null): Flow<List<BookEntry>> {
            TODO("3단계 Slice 3에서 구현")
        }
    }
