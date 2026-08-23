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
        operator fun invoke(isbn: String): Flow<Book?> = bookRepository.observeBook(isbn)

        suspend fun refresh(isbn: String): Result<Unit> = bookRepository.syncBook(isbn)
    }
