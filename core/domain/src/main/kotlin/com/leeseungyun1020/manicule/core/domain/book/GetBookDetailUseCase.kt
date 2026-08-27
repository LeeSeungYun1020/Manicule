package com.leeseungyun1020.manicule.core.domain.book

import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import com.leeseungyun1020.manicule.core.data.repository.LibraryRepository
import com.leeseungyun1020.manicule.core.model.BookDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetBookDetailUseCase
    @Inject
    constructor(
        private val bookRepository: BookRepository,
        private val libraryRepository: LibraryRepository,
    ) {
        operator fun invoke(isbn: String): Flow<BookDetail?> =
            bookRepository.observeBook(isbn).combine(libraryRepository.observeBookEntry(isbn)) { book, entry ->
                book?.let {
                    BookDetail(
                        book = it,
                        entry = entry?.copy(book = it),
                    )
                }
            }

        suspend fun refresh(isbn: String): Result<Unit> = bookRepository.syncBook(isbn)
    }
