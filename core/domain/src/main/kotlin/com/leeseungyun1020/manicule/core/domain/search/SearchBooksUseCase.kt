package com.leeseungyun1020.manicule.core.domain.search

import androidx.paging.PagingData
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
         */
        operator fun invoke(query: String): Flow<PagingData<Book>> = bookRepository.searchBooks(query)
    }
