package com.leeseungyun1020.manicule.core.domain.search

import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.model.Book
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchBooksUseCase
    @Inject
    constructor(
        private val bookRepository: BookRepository,
    ) {
        /**
         * 키워드로 도서 검색.
         * TODO: 3단계 Slice 1에서 Flow<PagingData<Book>>으로 전환
         */
        operator fun invoke(query: String): Flow<List<Book>> {
            TODO("3단계 Slice 1에서 구현")
        }
    }
