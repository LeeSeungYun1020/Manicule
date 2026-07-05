package com.leeseungyun1020.manicule.core.domain.book

import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBookDetailUseCase
    @Inject
    constructor(
        private val bookRepository: BookRepository,
    ) {
        /**
         * ISBN으로 도서 상세 조회 (DB 우선, 없으면 네트워크 fetch).
         */
        operator fun invoke(isbn: String): Flow<Book?> {
            TODO("3단계 Slice 2에서 구현")
        }
    }
