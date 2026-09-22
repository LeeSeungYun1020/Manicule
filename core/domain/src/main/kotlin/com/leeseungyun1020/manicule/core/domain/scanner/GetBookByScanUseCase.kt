package com.leeseungyun1020.manicule.core.domain.scanner

import com.leeseungyun1020.manicule.core.data.repository.BookRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetBookByScanUseCase
    @Inject
    constructor(
        private val bookRepository: BookRepository,
    ) {
        suspend operator fun invoke(candidates: List<String>): String? {
            for (candidate in candidates) {
                lookupCandidate(candidate)?.let { return it }
            }
            return null
        }

        private suspend fun lookupCandidate(candidate: String): String? =
            try {
                bookRepository.observeBook(candidate).first()?.isbn
                    ?: run {
                        bookRepository.syncBook(candidate).getOrThrow().book.isbn
                    }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                null
            }
    }
