package com.leeseungyun1020.manicule.core.domain.search

import androidx.paging.PagingData
import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.model.Book
import com.leeseungyun1020.manicule.core.model.Isbn
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchBooksUseCase
    @Inject
    constructor(
        private val bookRepository: BookRepository,
    ) {
        /**
         * 검색어로 도서 검색.
         * 유효한 ISBN 형식(ISBN-10 또는 ISBN-13 및 체크섬 일치)인 경우 정규화된 ISBN 전용 검색을 수행하고,
         * 그 외에는 일반 키워드(제목/저자) 검색을 수행한다.
         */
        operator fun invoke(query: String): Flow<PagingData<Book>> {
            val isbn = Isbn.parseOrNull(query)
            return if (isbn != null) {
                bookRepository.searchBooksByIsbn(isbn.value)
            } else {
                bookRepository.searchBooks(query)
            }
        }
    }
